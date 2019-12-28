import sys
import os
import subprocess
from subprocess import Popen, PIPE
import time
import json

assert len(sys.argv) == 2

# how long to run the server in seconds
LIFE_TIME = int(sys.argv[1])

os.chdir('/home/ubuntu/server')

p = Popen(['./run_silent.sh'], stdin=PIPE)

the_ip = None

for j in range(0, 10):

  time.sleep(6)

  p2 = Popen(['./ngrok_silent.sh'])

  for i in range(0, 10):

    time.sleep(6)

    result = subprocess.run(['curl', '-s', 'localhost:4040/api/tunnels'], stdout=subprocess.PIPE)

    try:

      res_str = result.stdout.decode('utf-8')
      data = json.loads(res_str)
    except:

      pass
    else:

      if data['tunnels']:

        the_ip = data['tunnels'][0]['public_url'][6:]
        break
      # end if
    # end else
  # end for

  if the_ip:

    break

  p2.kill()
# end for

if the_ip:

  subprocess.run(['python3', '../repo.py', the_ip])
  time.sleep(LIFE_TIME)
  subprocess.run(['python3', '../retro_repo.py'])

try:

  p.communicate(input=bytes('stop\n', 'utf-8'), timeout=5 * 60)
except subprocess.TimeoutExpired:

  p.terminate()
  # give extra time to shutdown. otherwise, kill p via shutdown
  time.sleep(5 * 60)

# skip p2, kill p2 via shutdown

subprocess.run(['sudo', 'shutdown', '-h', '+0'])
