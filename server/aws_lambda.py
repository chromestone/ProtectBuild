import boto3
import json

region = 'YOUR_REGION_HERE'
instances = ['YOUR_ID_HERE']
ec2 = boto3.client('ec2', region_name=region)

def lambda_handler(event, context):
    ec2.start_instances(InstanceIds=instances)
    return {
        'statusCode': 200
    }
