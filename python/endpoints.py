from __future__ import print_function
import logging
import os

import grpc
from google.protobuf.json_format import MessageToJson
from google.protobuf import duration_pb2

from kaikosdk import sdk_pb2_grpc
from kaikosdk.core import instrument_criteria_pb2, assets_pb2
from kaikosdk.stream.aggregates_ohlcv_v1 import request_pb2 as pb_ohlcv
from kaikosdk.stream.aggregates_vwap_v1 import request_pb2 as pb_vwap
from kaikosdk.stream.market_update_v1 import request_pb2 as pb_market_update
from kaikosdk.stream.market_update_v1 import commodity_pb2 as pb_commodity
from kaikosdk.stream.trades_v1 import request_pb2 as pb_trades
from kaikosdk.stream.index_v1 import request_pb2 as pb_index
from kaikosdk.stream.index_multi_assets_v1 import request_pb2 as pb_index_multi_assets
from kaikosdk.stream.index_forex_rate_v1 import request_pb2 as pb_index_forex_rate
from kaikosdk.stream.aggregated_quote_v2 import request_pb2 as pb_aggregated_quote
from kaikosdk.stream.aggregates_spot_exchange_rate_v2 import request_pb2 as pb_spot_exchange_rate
from kaikosdk.stream.aggregates_direct_exchange_rate_v2 import request_pb2 as pb_direct_exchange_rate
from kaikosdk.stream.derivatives_instrument_metrics_v1 import request_pb2 as pb_derivatives_instrument_metrics
from kaikosdk.stream.iv_svi_parameters_v1 import request_pb2 as pb_iv_svi_parameters
from kaikosdk.stream.exotic_indices_v1 import request_pb2 as pb_exotic_indices

def ohlcv_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatesOHLCVServiceV1Stub(channel)
            responses = stub.Subscribe(pb_ohlcv.StreamAggregatesOHLCVRequestV1(
                aggregate='1s',
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "cbse",
                    instrument_class = "spot",
                    code = "btc-usd"
                )
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def vwap_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatesVWAPServiceV1Stub(channel)
            responses = stub.Subscribe(pb_vwap.StreamAggregatesVWAPRequestV1(
                aggregate='1s',
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "binc",
                    instrument_class = "spot",
                    code = "eth-usdt"
                )
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def market_update_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamMarketUpdateServiceV1Stub(channel)
            # Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
            responses = stub.Subscribe(pb_market_update.StreamMarketUpdateRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "cbse",
                    instrument_class = "spot",
                    code = "*"
                ),
                commodities=[pb_commodity.SMUC_TRADE]
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def trades_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamTradesServiceV1Stub(channel)
            # Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
            responses = stub.Subscribe(pb_trades.StreamTradesRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "cbse",
                    instrument_class = "spot",
                    code = "btc-usd"
                )
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def index_rate_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamIndexServiceV1Stub(channel)
            responses = stub.Subscribe(pb_index.StreamIndexServiceRequestV1(
                index_code = "KK_BRR_BTCUSD" 
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def index_multi_asset(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamIndexMultiAssetsServiceV1Stub(channel)
            responses = stub.Subscribe(pb_index_multi_assets.StreamIndexMultiAssetsServiceRequestV1(
                index_code = "KT15"
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def index_forex_rate(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamIndexForexRateServiceV1Stub(channel)
            responses = stub.Subscribe(pb_index_forex_rate.StreamIndexForexRateServiceRequestV1(
                index_code = "KK_BRR_BTCUSD_EUR"
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def aggregated_quote_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatedQuoteServiceV2Stub(channel)
            # Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
            responses = stub.Subscribe(pb_aggregated_quote.StreamAggregatedQuoteRequestV2(
                instrument_class = "spot",
                code = "btc-usd"
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def aggregates_spot_exchange_rate_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatesSpotExchangeRateV2ServiceV1Stub(channel)

            window = duration_pb2.Duration()
            window.FromSeconds(10)

            update_frequency = duration_pb2.Duration()
            update_frequency.FromSeconds(2)

            responses = stub.Subscribe(pb_spot_exchange_rate.StreamAggregatesSpotExchangeRateV2RequestV1(
                assets = assets_pb2.Assets(
                    base = "btc",
                    quote = "usd"
                ),
                window = window,
                update_frequency = update_frequency
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def aggregates_direct_exchange_rate_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatesSpotDirectExchangeRateV2ServiceV1Stub(channel)

            window = duration_pb2.Duration()
            window.FromSeconds(10)

            update_frequency = duration_pb2.Duration()
            update_frequency.FromSeconds(2)

            responses = stub.Subscribe(pb_direct_exchange_rate.StreamAggregatesDirectExchangeRateV2RequestV1(
                assets = assets_pb2.Assets(
                    base = "btc",
                    quote = "usd"
                ),
                window = window,
                update_frequency = update_frequency
            ))
            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True))) 
    except grpc.RpcError as e:
        print(e.details(), e.code())

def derivatives_instrument_metrics_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamDerivativesInstrumentMetricsServiceV1Stub(channel)

            responses = stub.Subscribe(pb_derivatives_instrument_metrics.StreamDerivativesInstrumentMetricsRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "*",
                    instrument_class = "perpetual-future",
                    code = "btc-usd"
                )
            ))
            for response in responses:
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def iv_svi_parameters_v1_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamIvSviParametersServiceV1Stub(channel)

            responses = stub.Subscribe(pb_iv_svi_parameters.StreamIvSviParametersRequestV1(
                assets = assets_pb2.Assets(
                    base = "btc",
                    quote = "usd"
                ),
                exchanges = "drbt"
            ))
            for response in responses:
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())


def exotic_indices_v1_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamExoticIndicesServiceV1Stub(channel)

            responses = stub.Subscribe(pb_exotic_indices.StreamExoticIndicesServiceRequestV1(
                index_code = "KT10TCUSD",
            ))
            for response in responses:
                print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
    except grpc.RpcError as e:
        print(e.details(), e.code())


def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)

    # WARN: don't use `MessageToJson` in a tight read loop with endpoints like market update, it's eating CPU and can't keep up with the stream

    trades_request(channel)
    # ohlcv_request(channel)
    # vwap_request(channel)
    # index_rate_request(channel)
    # index_multi_asset(channel)
    # index_forex_rate(channel)
    # aggregated_quote_request(channel)
    # market_update_request(channel)
    # aggregates_spot_exchange_rate_request(channel)
    # aggregates_direct_exchange_rate_request(channel)
    # derivatives_instrument_metrics_request(channel)
    # iv_svi_parameters_v1_request(channel)
    # exotic_indices_v1_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()
