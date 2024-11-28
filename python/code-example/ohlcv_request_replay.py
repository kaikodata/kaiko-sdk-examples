# This is a code example. Configure your parameters below #

from __future__ import print_function
from datetime import datetime, timedelta
import logging
import os
from google.protobuf.timestamp_pb2 import Timestamp
import grpc
from google.protobuf.json_format import MessageToJson
from kaikosdk import sdk_pb2_grpc
from kaikosdk.core import instrument_criteria_pb2
from kaikosdk.stream.aggregates_ohlcv_v1 import request_pb2 as pb_ohlcv

def ohlcv_request(channel: grpc.Channel):
    try:
        # start of date configuration #
        start = Timestamp()
        start.FromDatetime(datetime.utcnow() - timedelta(days=2))
        end = Timestamp()
        end.FromDatetime(datetime.utcnow() - timedelta(days=1))
        # end of date configuration #
        
        stub = sdk_pb2_grpc.StreamAggregatesOHLCVServiceV1Stub(channel)
        responses = stub.Subscribe(pb_ohlcv.StreamAggregatesOHLCVRequestV1(
            aggregate='1s',
            instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                exchange = "cbse",
                instrument_class = "spot",
                code = "btc-usd"
            ),
                interval={
                    'start_time': start,
                    'end_time': end
                }
            ))
        for response in responses:
            # for debug purpose only, don't use MessageToJson in the reading loop in production
            print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))
                # print("Received message %s" % list(map(lambda o: o.string_value, response.data.values)))
    except grpc.RpcError as e:
        print(e.details(), e.code())

def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)
    ohlcv_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()