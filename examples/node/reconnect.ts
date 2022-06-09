import * as grpc from '@grpc/grpc-js';

import { InstrumentCriteria } from '@kaiko-data/sdk-node/sdk/core/instrument_criteria_pb';
import {
    StreamTradesServiceV1Client,
} from '@kaiko-data/sdk-node/sdk/sdk_grpc_pb';
import { StreamTradesRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/trades_v1/request_pb';
import { StreamTradesResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/trades_v1/response_pb';
import { BehaviorSubject, distinctUntilChanged, from, fromEvent, switchMap } from 'rxjs';
import { backOff } from "exponential-backoff";

const main = () => {

    // Setup authentication
    const token = process.env.KAIKO_API_KEY || '1234'; // Put your api key here
    const metaCallback = (_params: unknown, callback: (err: Error | null, metadata?: grpc.Metadata) => void) => {
        const meta = new grpc.Metadata();
        meta.add('Authorization', `Bearer ${token}`);
        callback(null, meta);
    };

    const channelCreds = grpc.credentials.createSsl() as any;
    const callCreds = grpc.credentials.createFromMetadataGenerator(metaCallback);
    const creds = grpc.credentials.combineCallCredentials(channelCreds, callCreds);

    // Create a request for streaming trades with SDK
    tradeRequest(creds);
}

const tradeRequest = async (creds: grpc.CallCredentials) => {
    const client = new StreamTradesServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamTradesRequestV1();

    const criteria = new InstrumentCriteria();
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    criteria.setExchange('cbse');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);

    // Run the request and get results
    const sub = new BehaviorSubject<grpc.ClientReadableStream<StreamTradesResponseV1>>(await subscribe(client, request));

    sub
        .pipe(
            distinctUntilChanged(), // avoid looping when subscription is updated
            switchMap(e => fromEvent(e, 'end')), // listen for end of stream event
            switchMap(_ => {
                console.log('[TRADE] Resubscribing after end of stream');
                return from(subscribe(client, request)); // resubscribe when 'end' event is emitted
            })
        )
        .subscribe(sub); // trigger next listens
}

const subscribe = async (client: StreamTradesServiceV1Client, request: StreamTradesRequestV1) => {

    const subscription = await backOff<grpc.ClientReadableStream<StreamTradesResponseV1>>(() => new Promise((resolve, reject) => {
        try {
            const s = client.subscribe(request)
            resolve(s);
        } catch (e) {
            reject(e);
        }
    }), {
        delayFirstAttempt: true,
        jitter: 'full',
        numOfAttempts: 10,
        startingDelay: 2000 // 2 seconds
    });

    subscription.on('data', (response: StreamTradesResponseV1) => {
        console.log(`[TRADE] code: ${response.getCode()}, price: ${response.getPrice()}`);
    });

    subscription.on('end', () => {
        console.log('[TRADE] Stream ended')
    });

    subscription.on('error', (error: grpc.ServiceError) => {
        console.error(error);
    });

    console.log('[TRADE] Stream started')

    return subscription;
}

main();
