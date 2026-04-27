import csv

with open("bulk_users_excel.csv", "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    writer.writerow(["email", "password"])
    for i in range(1, 10001):
        writer.writerow([f"test{i}@gmail.com", "123456"])