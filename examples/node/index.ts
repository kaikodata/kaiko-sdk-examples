import { StreamAggregatesOHLCVServiceV1Client } from '@kaiko-sdk/node/sdk/sdk_grpc_pb';
import { InstrumentCriteria } from '@kaiko-sdk/node/sdk/core/instrument_criteria_pb';
import { StreamAggregatesOHLCVRequestV1 } from '@kaiko-sdk/node/sdk/stream/aggregates_ohlcv_v1/request_pb';
import { StreamAggregatesOHLCVResponseV1 } from '@kaiko-sdk/node/sdk/stream/aggregates_ohlcv_v1/response_pb';

import * as grpc from '@grpc/grpc-js';

const main = () => {

    // Setup authentication
    const token = process.env.KAIKO_API_KEY || '1234'; // Put your api key here
    const metaCallback = (_params:unknown, callback:(err: Error | null, metadata?: grpc.Metadata) => void) => {
        const meta = new grpc.Metadata();
        meta.add('Authorization', `Bearer ${token}`);
        callback(null, meta);
    };

    const channelCreds = grpc.credentials.createSsl() as any;
    const callCreds = grpc.credentials.createFromMetadataGenerator(metaCallback);
    const creds = grpc.credentials.combineCallCredentials(channelCreds, callCreds);
    const client = new StreamAggregatesOHLCVServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);

    // Create a request with SDK
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

main();
