kill -9 $(ps aux | grep python | grep -v grep | awk '{print $2}')
python3 -m http.server 3000 --directory admin-panel > /dev/null 2>&1 &
