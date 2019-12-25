import sys
import os
import tempfile
import zipfile
from git import Repo

assert len(sys.argv) == 2

os.chdir('/home/ubuntu')

with tempfile.TemporaryDirectory() as tmpdirname:

  with zipfile.ZipFile('./repo.zip', 'r') as the_zip:

    the_zip.extractall(tmpdirname)

  where_repo_is = os.path.join(tmpdirname, 'ProtectBuild')
  where_ip_is = os.path.join(where_repo_is, 'ip.txt')

  with open(where_ip_is, 'w+') as fp:

    fp.write(sys.argv[1])
    fp.write('\n')

  repo = Repo(where_repo_is)
  assert not repo.bare

  rgit = repo.git
  rgit.add('ip.txt')
  rgit.commit('-m', 'Update ip.txt')
  rgit.push('origin', 'gh-pages', '--force')
