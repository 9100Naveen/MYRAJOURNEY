Deployment Guide (PHP + MySQL)

Requirements
- PHP 8.1+ (extensions: pdo_mysql, mbstring, json, openssl, fileinfo)
- Web server: Apache (mod_php) or Nginx + PHP-FPM
- MySQL 8.x
- HTTPS certificate (Let’s Encrypt recommended)

Directory layout
- Document root -> backend/public/
- Writable: backend/public/uploads, backend/storage/uploads

Environment
Create backend/.env (same level as src/) with:

DB_HOST=127.0.0.1
DB_NAME=myrajourney
DB_USER=root
DB_PASS=change_me
JWT_SECRET=change_me_long_random
JWT_TTL_SECONDS=604800
CORS_ORIGINS=*

Database
- Create DB: CREATE DATABASE myrajourney CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
- Import migrations in order:
  - 001_users.sql → 010_symptoms_metrics.sql

Apache (VirtualHost)
<VirtualHost *:80>
    ServerName api.example.com
    DocumentRoot /var/www/myrajourney/backend/public

    <Directory /var/www/myrajourney/backend/public>
        AllowOverride All
        Require all granted
    </Directory>

    ErrorLog ${APACHE_LOG_DIR}/myrajourney_error.log
    CustomLog ${APACHE_LOG_DIR}/myrajourney_access.log combined
</VirtualHost>

.htaccess (already added)
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^ index.php [QSA,L]

Nginx (server block)
server {
    listen 80;
    server_name api.example.com;

    root /var/www/myrajourney/backend/public;
    index index.php;

    location / {
        try_files $uri /index.php?$query_string;
    }

    location ~ \.php$ {
        include fastcgi_params;
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
        fastcgi_pass unix:/run/php/php8.1-fpm.sock;
    }
}

Permissions
sudo chown -R www-data:www-data backend/public/uploads backend/storage/uploads
sudo find backend/public/uploads -type d -exec chmod 775 {} \;

Security
- Force HTTPS (redirect 80 → 443)
- Set CORS origins to your app’s domain(s)
- Keep JWT_SECRET private; rotate periodically
- Limit upload mime types (already enforced)

Backups
- Nightly mysqldump
- Weekly rsync/snapshot of backend/public/uploads and backend/storage/uploads

Monitoring/Logs
- Web server access/error logs
- PHP error log

Smoke tests (after deploy)
- POST /api/v1/auth/register → 201
- POST /api/v1/auth/login → 200 with token
- GET /api/v1/auth/me (Bearer) → user
- POST /api/v1/reports (multipart) → 201




















