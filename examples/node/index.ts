import { StreamAggregatesOHLCVServiceV1Client, StreamTradesServiceV1Client } from '@kaiko-sdk/node/sdk/sdk_grpc_pb';
import { InstrumentCriteria } from '@kaiko-sdk/node/sdk/core/instrument_criteria_pb';
import { StreamAggregatesOHLCVRequestV1 } from '@kaiko-sdk/node/sdk/stream/aggregates_ohlcv_v1/request_pb';
import { StreamAggregatesOHLCVResponseV1 } from '@kaiko-sdk/node/sdk/stream/aggregates_ohlcv_v1/response_pb';

import * as grpc from '@grpc/grpc-js';
import { StreamTradesResponseV1 } from '@kaiko-sdk/node/sdk/stream/trades_v1/response_pb';
import { StreamTradesRequestV1 } from '@kaiko-sdk/node/sdk/stream/trades_v1/request_pb';

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

    // Create a request for streaming ohlcv with SDK
    ohlcvRequest(creds);
}

const ohlcvRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamAggregatesOHLCVServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamAggregatesOHLCVRequestV1();

    const criteria = new InstrumentCriteria();
    criteria.setExchange('cbse');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);
    request.setAggregate('1s');

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamAggregatesOHLCVResponseV1) => {
        console.log(`aggregate: ${response.getAggregate()}, code: ${response.getCode()}, close: ${response.getClose()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const tradeRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamTradesServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamTradesRequestV1();

    const criteria = new InstrumentCriteria();
    criteria.setExchange('cbse');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamTradesResponseV1) => {
        console.log(`code: ${response.getCode()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

main();
