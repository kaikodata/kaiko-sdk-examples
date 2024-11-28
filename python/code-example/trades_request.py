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
from kaikosdk.stream.trades_v1 import request_pb2 as pb_trades

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

def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)
    trades_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()