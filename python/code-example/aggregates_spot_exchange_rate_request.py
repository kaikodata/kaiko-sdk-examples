# This is a code example. Configure your parameters below #

from __future__ import print_function
from datetime import datetime, timedelta
import logging
import os
from google.protobuf.timestamp_pb2 import Timestamp
import grpc
from google.protobuf.json_format import MessageToJson
from google.protobuf import duration_pb2
from kaikosdk import sdk_pb2_grpc
from kaikosdk.core import assets_pb2
from kaikosdk.stream.aggregates_spot_exchange_rate_v2 import request_pb2 as pb_spot_exchange_rate

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

def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)
    aggregates_spot_exchange_rate_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()