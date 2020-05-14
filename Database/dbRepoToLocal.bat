set CURR_PATH=%cd%
set MONGODB_PATH=C:\Program Files\MongoDB\Server\4.2\bin
cd %MONGODB_PATH%
%MONGODB_PATH:~0,2%
mongorestore --drop -d guideDB %CURR_PATH%\data
