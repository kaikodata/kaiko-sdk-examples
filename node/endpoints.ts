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
    StreamIndexMultiAssetsServiceV1Client,
    StreamIndexForexRateServiceV1Client,
    StreamAggregatedQuoteServiceV2Client,
    StreamAggregatesSpotExchangeRateV2ServiceV1Client,
    StreamAggregatesSpotDirectExchangeRateV2ServiceV1Client,
    StreamDerivativesInstrumentMetricsServiceV1Client,
    StreamIvSviParametersServiceV1Client,
    StreamExoticIndicesServiceV1Client
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
import { StreamIndexMultiAssetsServiceRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/index_multi_assets_v1/request_pb';
import { StreamIndexMultiAssetsServiceResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/index_multi_assets_v1/response_pb';
import { StreamIndexForexRateServiceRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/index_forex_rate_v1/request_pb';
import { StreamIndexForexRateServiceResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/index_forex_rate_v1/response_pb';
import { StreamAggregatedQuoteRequestV2 } from '@kaiko-data/sdk-node/sdk/stream/aggregated_quote_v2/request_pb';
import { StreamAggregatedQuoteResponseV2 } from '@kaiko-data/sdk-node/sdk/stream/aggregated_quote_v2/response_pb';
import { StreamAggregatesSpotExchangeRateV2RequestV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_spot_exchange_rate_v2/request_pb';
import { StreamAggregatesDirectExchangeRateV2RequestV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_direct_exchange_rate_v2/request_pb';
import { StreamAggregatesDirectExchangeRateV2ResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_direct_exchange_rate_v2/response_pb';
import { StreamAggregatesSpotExchangeRateV2ResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/aggregates_spot_exchange_rate_v2/response_pb';
import { Assets } from '@kaiko-data/sdk-node/sdk/core/assets_pb';
import { Duration } from 'google-protobuf/google/protobuf/duration_pb';
import { StreamDerivativesInstrumentMetricsRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/derivatives_instrument_metrics_v1/request_pb';
import { StreamDerivativesInstrumentMetricsResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/derivatives_instrument_metrics_v1/response_pb';
import { StreamIvSviParametersRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/iv_svi_parameters_v1/request_pb';
import { StreamIvSviParametersResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/iv_svi_parameters_v1/response_pb';
import { StreamExoticIndicesServiceRequestV1 } from '@kaiko-data/sdk-node/sdk/stream/exotic_indices_v1/request_pb';
import { StreamExoticIndicesServiceResponseV1 } from '@kaiko-data/sdk-node/sdk/stream/exotic_indices_v1/response_pb';
import { StreamIndexCommodity } from '@kaiko-data/sdk-node/sdk/stream/index_v1/commodity_pb';

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

    // Create a request for streaming index rates with SDK
    indexRateRequest(creds);

    // Create a request for streaming index forex rates with SDK
    indexForexRateRequest(creds);

    // Create a request for streaming index multi assets with SDK
    indexMultiAssetRequest(creds);

    // Create a request for stream aggregated quote with SDK
    aggregatedQuoteRequest(creds);

    // Create a request for stream spot exchange rate with SDK
    aggregatesSpotExchangeRateRequest(creds);

    // Create a request for stream spot direct exchange rate with SDK
    aggregatesSpotDirectExchangeRateRequest(creds);

    // Create a request for stream derivatives instrument metrics with SDK
    derivativesInstrumentMetricsRequest(creds);

    // Create a request for stream iv svi parameters with SDK
    ivSviParametersRequest(creds);

    // Create an exotic indices request with SDK
    exoticIndicesRequest(creds);
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
    criteria.setExchange('binc');
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

const indexRateRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamIndexServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamIndexServiceRequestV1();

    request.setIndexCode("KK_BRR_BTCUSD");

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamIndexServiceResponseV1) => {
        console.log(`[INDEX_RATE] indexCode: ${response.getIndexCode()}, commodity: ${response.getCommodity()}, price: ${response.getPercentagesList()?.map((e) => e.getPrice())}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[INDEX_RATE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const indexMultiAssetRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamIndexMultiAssetsServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamIndexMultiAssetsServiceRequestV1();

    request.setIndexCode("KT15");

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamIndexMultiAssetsServiceResponseV1) => {
        console.log(`[MULTI_INDEX] indexCode: ${response.getIndexCode()}, commodity: ${response.getCommodity()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[MULTI_INDEX] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const indexForexRateRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamIndexForexRateServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamIndexForexRateServiceRequestV1();

    request.setIndexCode("KK_BRR_BTCUSD_EUR");

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamIndexForexRateServiceResponseV1) => {
        console.log(`[INDEX_FOREX_RATE] indexCode: ${response.getIndexCode()}, commodity: ${response.getCommodity()}, price: ${response.getPrice()}`);
        // console.log(response);
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[INDEX_FOREX_RATE] Stream ended')
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

const aggregatesSpotExchangeRateRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamAggregatesSpotExchangeRateV2ServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamAggregatesSpotExchangeRateV2RequestV1();

    const assets = new Assets();
    assets.setBase("btc");
    assets.setQuote("usd");

    request.setAssets(assets);
    request.setWindow(new Duration().setSeconds(10));
    request.setUpdateFrequency(new Duration().setSeconds(2));

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamAggregatesSpotExchangeRateV2ResponseV1) => {
        const value = response.getPrice();
        if (value) {
            console.log(`[SPOT EXCHANGE RATE] code: ${response.getAssets()}, price: ${JSON.stringify(value)}}`);
        }
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[SPOT EXCHANGE RATE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const aggregatesSpotDirectExchangeRateRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamAggregatesSpotDirectExchangeRateV2ServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamAggregatesDirectExchangeRateV2RequestV1();

    const assets = new Assets();
    assets.setBase("btc");
    assets.setQuote("usd");

    request.setAssets(assets);
    request.setWindow(new Duration().setSeconds(10));
    request.setUpdateFrequency(new Duration().setSeconds(2));

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamAggregatesDirectExchangeRateV2ResponseV1) => {
        const value = response.getPrice();
        if (value) {
            console.log(`[SPOT DIRECT EXCHANGE RATE] code: ${response.getAssets()}, price: ${JSON.stringify(value)}}`);
        }
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[SPOT DIRECT EXCHANGE RATE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const derivativesInstrumentMetricsRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamDerivativesInstrumentMetricsServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamDerivativesInstrumentMetricsRequestV1();

    const criteria = new InstrumentCriteria();
    criteria.setExchange('*');
    criteria.setInstrumentClass('perpetual-future');
    criteria.setCode('btc-usd');

    request.setInstrumentCriteria(criteria);

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamDerivativesInstrumentMetricsResponseV1) => {
        const value = response.getValue();
        if (value) {
            console.log(`[DERIVATIVES INSTRUMENT METRICS] commodity: ${response.getCommodity()}, value: ${JSON.stringify(value)}}`);
        }
        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[DERIVATIVES INSTRUMENT METRICS] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const ivSviParametersRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamIvSviParametersServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamIvSviParametersRequestV1();

    const assets = new Assets();
    assets.setBase('btc');
    assets.setQuote('usd');

    request.setAssets(assets);
    request.setExchanges("drbt");

    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamIvSviParametersResponseV1) => {
        console.log(`[IV SVI PARAMETERS] value: ${JSON.stringify(response.toObject())}}`);

        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[IV SVI PARAMETERS] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}

const exoticIndicesRequest = (creds: grpc.CallCredentials): void => {
    const client = new StreamExoticIndicesServiceV1Client('gateway-v0-grpc.kaiko.ovh:443', creds as any);
    const request = new StreamExoticIndicesServiceRequestV1();

    request.setIndexCode("KT10TCUSD")
    request.setCommoditiesList([StreamIndexCommodity.SIC_REAL_TIME])
    
    // Run the request and get results
    const call = client.subscribe(request);

    let count = 0;
    call.on('data', (response: StreamExoticIndicesServiceResponseV1) => {
        console.log(`[EXOTIC INDICE] value: ${JSON.stringify(response.toObject())}}`);

        count++;
        if (count >= 5) {
            call.cancel();
        }
    });

    call.on('end', () => {
        console.log('[EXOTIC INDICE] Stream ended')
    });

    call.on('error', (error: grpc.ServiceError) => {
        if (error.code === grpc.status.CANCELLED) { return; }
        console.error(error);
    })
}


main();
