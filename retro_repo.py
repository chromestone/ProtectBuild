import os
import tempfile
import zipfile
from git import Repo

os.chdir('/home/ubuntu')

with tempfile.TemporaryDirectory() as tmpdirname:

  with zipfile.ZipFile('./repo.zip', 'r') as the_zip:

    the_zip.extractall(tmpdirname)

  where_repo_is = os.path.join(tmpdirname, 'ProtectBuild')
  repo = Repo(where_repo_is)
  assert not repo.bare

  rgit = repo.git
  rgit.push('origin', 'gh-pages', '--force')
