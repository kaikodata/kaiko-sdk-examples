from __future__ import print_function
import logging
import os

import grpc
from google.protobuf.json_format import MessageToJson
from retrying import retry

from kaikosdk import sdk_pb2_grpc
from kaikosdk.core import instrument_criteria_pb2
from kaikosdk.stream.trades_v1 import request_pb2 as pb_trades

def trades_request(channel: grpc.Channel):
    try:
        with channel:
            stub = sdk_pb2_grpc.StreamTradesServiceV1Stub(channel)
            subscribe(stub)
    except grpc.RpcError as e:
        print(e.details(), e.code())

def retry_if_eof_error(exception):
    should_retry = isinstance(exception, EOFError)
    if should_retry:
        print("[TRADES] Resubscribing")
    
    return should_retry

@retry(retry_on_exception=retry_if_eof_error, wait_fixed=2000)
def subscribe(stub: sdk_pb2_grpc.StreamTradesServiceV1Stub):
    # Globbing patterns are also supported on all fields. See http://sdk.kaiko.com/#instrument-selection for all supported patterns
    print("[TRADES] Stream started")
    responses = stub.Subscribe(pb_trades.StreamTradesRequestV1(
                instrument_criteria = instrument_criteria_pb2.InstrumentCriteria(
                    exchange = "cbse",
                    instrument_class = "spot",
                    code = "btc-usd"
                )
            ))
    for response in responses:
        print("Received message %s" % (MessageToJson(response, including_default_value_fields = True)))

    print("[TRADES] Stream ended")
    raise EOFError("End of stream")

def run():
    credentials = grpc.ssl_channel_credentials(root_certificates=None)
    call_credentials = grpc.access_token_call_credentials(os.environ['KAIKO_API_KEY'])
    composite_credentials = grpc.composite_channel_credentials(credentials, call_credentials)
    channel = grpc.secure_channel('gateway-v0-grpc.kaiko.ovh', composite_credentials)

    trades_request(channel)

if __name__ == '__main__':
    logging.basicConfig()
    run()
