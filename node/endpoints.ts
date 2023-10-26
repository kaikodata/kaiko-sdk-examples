import * as grpc from '@grpc/grpc-js';

import { InstrumentCriteria } from '@kaiko-data/sdk-node/sdk/core/instrument_criteria_pb';
import { StreamAggregatesOHLCVRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_ohlcv_v1/request_pb';
import { StreamAggregatesOHLCVResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_ohlcv_v1/response_pb';
import {
    StreamAggregatesOHLCVServiceV1Client,
    StreamAggregatesVWAPServiceV1Client,
    StreamMarketUpdateServiceV1Client,
    StreamTradesServiceV1Client,
    StreamIndexServiceV1Client,
    StreamAggregatedQuoteServiceV2Client,
} from '@kaiko-data/sdk-node/sdk/sdk_grpc_pb';
import { StreamAggregatesVWAPRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_vwap_v1/request_pb';
import { StreamAggregatesVWAPResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_vwap_v1/response_pb';
import { StreamMarketUpdateRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/market_update_v1/request_pb';
import { StreamMarketUpdateResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/market_update_v1/response_pb';
import { StreamMarketUpdateCommodity } from '@kaiko-data/sdk-node/sdk/stream/market_update_v1/commodity_pb';
import { StreamTradesRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/trades_v1/request_pb';
import { StreamTradesResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/trades_v1/response_pb';
import { StreamIndexServiceRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/index_v1/request_pb';
import { StreamIndexServiceResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/index_v1/response_pb';
import { StreamAggregatedQuoteRequestV2 } from '@kaiko-data/sdk-node/sdk/stream/aggregated_quote_v2/request_pb';
import { StreamAggregatedQuoteResponseV2 } from '@kaiko-data/sdk-node/sdk/stream/aggregated_quote_v2/response_pb';

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

    // Create a request for streaming ohlcv with SDK
    ohlcvRequest(creds);

    // Create a request for streaming vwap with SDK
    vwapRequest(creds);

    // Create a request for streaming market updates with SDK
    marketUpdatesRequest(creds);

    // Create a request for streaming trades with SDK
    tradeRequest(creds);

    // Create a request for streaming index with SDK
    indexRequest(creds);

    // Create a request for stream aggregated quote with SDK
    aggregatedQuoteRequest(creds);
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
        console.log(`[OHLCV] aggregate: ${response.getAggregate()}, code: ${response.getCode()}, close: ${response.getClose()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[OHLCV] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const vwapRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamAggregatesVWAPServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamAggregatesVWAPRequestV1();

    const criteria = new InstrumentCriteria();
    criteria.setExchange('bnce');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);
    request.setAggregate('1s');

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamAggregatesVWAPResponseV1) => {
        console.log(`[VWAP] aggregate: ${response.getAggregate()}, code: ${response.getCode()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[VWAP] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const marketUpdatesRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamMarketUpdateServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamMarketUpdateRequestV1();

    const criteria = new InstrumentCriteria();
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    criteria.setExchange('krkn');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);
    request.addCommodities(StreamMarketUpdateCommodity.SMUC_TRADE);

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamMarketUpdateResponseV1) => {
        console.log(`[MARKET UPDATE] commodity: ${response.getCommodity()}, code: ${response.getCode()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[MARKET UPDATE] Stream ended')
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
    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    criteria.setExchange('cbse');
    criteria.setInstrumentClass('spot');
    criteria.setCode('*');

    request.setInstrumentCriteria(criteria);

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamTradesResponseV1) => {
        console.log(`[TRADE] code: ${response.getCode()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[TRADE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const indexRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamIndexServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamIndexServiceRequestV1();

    request.setIndexCode("index_code"); // fill it with actual value

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamIndexServiceResponseV1) => {
        console.log(`[INDEX] indexCode: ${response.getIndexCode()}, commodity: ${response.getCommodity()}, percentage: ${response.getPercentagesList()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[INDEX] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}


const aggregatedQuoteRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamAggregatedQuoteServiceV2Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamAggregatedQuoteRequestV2();

    // Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    request.setInstrumentClass("spot");
    request.setCode("btc-usd");

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamAggregatedQuoteResponseV2) => {
        const value = response.getVetted();
        if (value) {
            console.log(`[AGGREGATED QUOTE] code: ${response.getCode()}, price: ${JSON.stringify(value.getPrice())}, volume: ${JSON.stringify(value.getVolume())}`);
        }
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[AGGREGATED QUOTE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

main();
