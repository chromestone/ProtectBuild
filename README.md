# ProtectBuild
A Spigot Minecraft Plugin

This is how I run my "private" server for 6 hours a day on AWS.

First, the CloudWatch rule using the cron expression in cron.txt (in UTC).

Second, the lambda function that gets triggered in aws_lambda.py .

Third, the user data triggers schedule.py on the instance at every boot. The user data is in server_script.txt and schedule.py automates everything.

Note that schedule.py shutsdown the server (last line of code).

**Requirements**

Ngrok is downloaded and ngrok needs to be setup under the correct user and have the authentication token stored.

Python needs to have GitPython installed.

The repo.zip should be the zip of a single branch clone of a repo that you don't mind force pushing to all the time. I have hardcoded to assume the folder that unzipping makes to be "ProtectBuild"; change this accordingly.

Note: I used ssh clone for the repo and GitHub deploy key for authentication.

**Resources**

https://ngrok.com/

https://aws.amazon.com/premiumsupport/knowledge-center/start-stop-lambda-cloudwatch/

https://aws.amazon.com/premiumsupport/knowledge-center/execute-user-data-ec2/

https://stackoverflow.com/questions/1778088/how-do-i-clone-a-single-branch-in-git

```
git clone <url> --branch <branch> --single-branch [<folder>]
```

https://developer.github.com/v3/guides/managing-deploy-keys/#deploy-keys
