import sqlite3

dbname = 'sql/usser.db'

conn = sqlite3.connect(dbname)
c = conn.cursor()

# executeメソッドでSQL文を実行する
create_table = '''create table trompot_team (USERNAME text, PASSMD5 text, PERMIT text, NAME text, TEL numeric)'''
c.execute(create_table)

# SQL文に値をセットする場合は，Pythonのformatメソッドなどは使わずに，
# セットしたい場所に?を記述し，executeメソッドの第2引数に?に当てはめる値を
# タプルで渡す．
sql = 'insert into trompot_team (USERNAME, PASSMD5, PERMIT, NAME, TEL) values (?,?,?,?,?)'
values = [
('taro_teacher', 'djfosfdhgoidfsuglkdfjsglj', 'ADMIN', '太郎先生', '114514'),
('mofu_teacher', 'djfosfdhgoidfsuglkdfjsglj', 'ADMIN', 'もふ先生', '1919810'),
('gero_teacher', 'djfosfdhgoidfsuglkdfjsglj', 'ADMIN', 'げろ先生', '4545'),
('hoge_teacher', 'djfosfdhgoidfsuglkdfjsglj', 'ADMIN', 'ほげ先生', '114514'),
]
for var in values:
	c.execute(sql, var)

conn.commit()

select_sql = 'select * from trompot_team'
for row in c.execute(select_sql):
    print(row)

conn.close()
