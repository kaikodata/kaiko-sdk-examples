from __future__ import print_function
import logging
import os

import grpc

from kaikosdk import sdk_pb2, sdk_pb2_grpc
from kaikosdk.core import instrument_criteria_pb2
from kaikosdk.stream.aggregates_ohlcv_v1 import request_pb2 as pb_ohlcv
from kaikosdk.stream.aggregates_spot_exchange_rate_v1 import request_pb2 as pb_spot_exchange_rate
from kaikosdk.stream.aggregates_vwap_v1 import request_pb2 as pb_vwap
from kaikosdk.stream.market_update_v1 import request_pb2 as pb_market_update
from kaikosdk.stream.market_update_v1 import commodity_pb2 as pb_commodity
from kaikosdk.stream.trades_v1 import request_pb2 as pb_trades

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
                print("Received message %s" % (response))
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
                    exchange = "bnce",
                    instrument_class = "spot",
                    code = "eth-usdt"
                )
            ))
            for response in responses:
                print("Received message %s" % (response))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def spot_exchange_rate_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamAggregatesSpotExchangeRateServiceV1Stub(channel)
            responses = stub.Subscribe(pb_spot_exchange_rate.StreamAggregatesSpotExchangeRateRequestV1(
                aggregate='1s',
                code='btc-usd',
                sources=False,
            ))
            for response in responses:
                print("Received message %s" % (response))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def market_update_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamMarketUpdateServiceV1Stub(channel)
            responses = stub.Subscribe(pb_market_update.StreamMarketUpdateRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "krkn",
                    instrument_class = "spot",
                    code = "*"
                ),
                commodities=[pb_commodity.SMUC_TRADE]
            ))
            for response in responses:
                print("Received message %s" % (response))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def trades_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamTradesServiceV1Stub(channel)
            responses = stub.Subscribe(pb_trades.StreamTradesRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "cbse",
                    instrument_class = "spot",
                    code = "btc-usd"
                )
            ))
            for response in responses:
                print("Received message %s" % (response))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)

    # trades_request(channel)
    # ohlcv_request(channel)
    # vwap_request(channel)
    # spot_exchange_rate_request(channel)
    market_update_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()
