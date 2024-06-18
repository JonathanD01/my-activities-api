-- Set user to enabled so we can authenticate

UPDATE users SET enabled = true WHERE email = 'static-email@spring.no';