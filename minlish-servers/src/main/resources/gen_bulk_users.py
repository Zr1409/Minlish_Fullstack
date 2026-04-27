
with open("e:/minlish-servers/src/main/resources/bulk_users.sql", "w", encoding="utf-8") as f:
    f.write("-- Tạo 10.000 user test từ test1@gmail.com đến test10000@gmail.com\n")
    f.write("INSERT INTO users (email, password, full_name, learning_goal, level, created_at, updated_at) VALUES\n")
    for i in range(1, 10001):
        line = f"('test{i}@gmail.com',  '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Test User {i}', 'IELTS', 'A1', NOW(), NOW())"
        if i < 10000:
            line += ",\n"
        else:
            line += ";\n"
        f.write(line)
#cd /d e:\minlish-servers\src\main\resources
#python gen_bulk_users_excel.py