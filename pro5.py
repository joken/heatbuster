import sqlite3
import json

class Database:
	db = None
	cursor = None
	
	# コンストラクタ
	def __init__(self, db_path):
		self.db     = sqlite3.connect(db_path)
		self.cursor = self.db.cursor()
		
	# デストラクタ
	def __del__(self):
		self.db.close()
		
	# ローカル用にコミットを関数化
	def commit(self):
		self.cursor.commit()

class UserDatabase(Database):
	# ユーザーの一覧の取得
	def getUserList(self, team_name):
		try:
			gerogero = {}
			select_sql = 'select * from {}'.format(team_name)
			for row in self.cursor.execute(select_sql):
				gerogero[row[0]] = {
					'name' : row[3],
					'tel' : row[4],
					'permission' : row[2],
				}

			return {
				"success" : "true",
				"result"  : gerogero,
			}
		except:
			return {
				"success" : "false",
			}
			

# ここから下はコントローラーだと思って！！！！！

from bottle import *

mofumofu = UserDatabase('/Users/NakanoYuki/Desktop/procon2016/sql/usser.db')

@route("/user/<team_name>/list")
def UserList(team_name):
	return json.dumps(mofumofu.getuserList(team_name), ensure_ascii=False)

@route("/user/<team_name>/add")
def UserAdd(team_name):
	pass
