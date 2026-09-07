INSERT INTO roles (name, display_name, description)
VALUES ('SUPER_ADMIN', 'Super Administrator', 'Full system access; can manage everything including other admins'),
       ('ADMIN', 'Administrator', 'Manages states, rates, and applications; cannot manage other admins'),
       ('USER', 'End User', 'Can submit own applications and make payments');
