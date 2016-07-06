# web.py

from bottle import *
from pro5 import *


mofumofu = UserDatabase('/Users/NakanoYuki/Desktop/procon2016/sql/usser.db')

@route("/user/<team_name>/list")
def UserList(team_name):
	return json.dumps(mofumofu.getUserList(team_name), ensure_ascii=False)

@route("/user/<team_name>/add")
def UserAdd(team_name):
	pass

# 起動する
run(host='localhost', port=8080, debug=True, reloader=True)

