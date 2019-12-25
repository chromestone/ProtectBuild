#! /usr/bin/env bash

# ngrok executable goes in the parent folder of server (current folder)

../ngrok tcp 25565 &> /dev/null
