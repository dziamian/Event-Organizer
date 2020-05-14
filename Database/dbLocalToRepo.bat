set CURR_PATH=%cd%
set MONGODB_PATH=C:\Program Files\MongoDB\Server\4.2\bin
cd %MONGODB_PATH%
%MONGODB_PATH:~0,2%
mongodump -d data -o %CURR_PATH%