# This is a code example. Configure your parameters below #

import logging
import os

import grpc
from google.protobuf.json_format import MessageToJson
from kaikosdk import sdk_pb2_grpc
from kaikosdk.core import instrument_criteria_pb2
from kaikosdk.stream.orderbookl2_v1 import request_pb2 as pb_orderbookl2


def to_json(message):
    # protobuf >= 5.26 renamed including_default_value_fields
    try:
        return MessageToJson(message, always_print_fields_with_no_presence=True)
    except TypeError:
        return MessageToJson(message, including_default_value_fields=True)


def orderbookl2_v1_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamOrderbookL2ServiceV1Stub(channel)

            responses = stub.Subscribe(
                pb_orderbookl2.StreamOrderBookL2RequestV1(
                    instrument_criteria=instrument_criteria_pb2.InstrumentCriteria(
                        exchange="binc",
                        instrument_class="perpetual-future",
                        code="btc-usdt",
                    ),
                )
            )

            for response in responses:
                # for debug purpose only, don't use MessageToJson in the reading loop in production
                print("Received message %s" % to_json(response))
    except grpc.RpcError as e:
        print(e.details(), e.code())


def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ["KAIKO_API_KEY"])
    composite_credentials = grpc.composite_channel_credentials(
        credentials, call_credentials
    )
    channel = grpc.secure_channel("gateway-v0-grpc.kaiko.ovh", composite_credentials)
    orderbookl2_v1_request(channel)


if __name__ == "__main__":
    logging.basicConfig()
    run()
