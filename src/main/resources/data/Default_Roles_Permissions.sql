-- SUPER_ADMIN — gets all 110 permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.name = 'SUPER_ADMIN';

-- ADMIN — full ops, no RBAC meta management (can't create/delete roles/permissions/endpoint-mappings)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
    -- Users (all except change-password for others)
                                          'USERS:CREATE', 'USERS:READ', 'USERS:UPDATE', 'USERS:SEARCH',
                                          'USERS:ENABLE', 'USERS:DISABLE', 'USERS:STATS',
    -- Roles (read only)
                                          'ROLES:READ', 'ROLES:SEARCH', 'ROLES:STATS',
    -- Permissions (read only)
                                          'PERMISSIONS:READ', 'PERMISSIONS:SEARCH', 'PERMISSIONS:READ_ACTIVE',
                                          'PERMISSIONS:STATS',
    -- Endpoint Permissions (read only)
                                          'ENDPOINT_PERMISSIONS:READ', 'ENDPOINT_PERMISSIONS:SEARCH',
                                          'ENDPOINT_PERMISSIONS:STATS',
    -- Analysis
                                          'ANALYSIS:REPORT',
    -- Payments
                                          'PAYMENTS:VERIFY_APPLICATION', 'PAYMENTS:VERIFY_AI_APPLICATION',
    -- Notifications
                                          'NOTIFICATIONS:READ', 'NOTIFICATIONS:COUNT_UNREAD', 'NOTIFICATIONS:MARK_READ',
                                          'NOTIFICATIONS:MARK_ALL_READ',
    -- Campaign Notifications
                                          'CAMPAIGN_NOTIFICATIONS:CREATE', 'CAMPAIGN_NOTIFICATIONS:READ',
                                          'CAMPAIGN_NOTIFICATIONS:SEARCH',
                                          'CAMPAIGN_NOTIFICATIONS:UPDATE', 'CAMPAIGN_NOTIFICATIONS:DELETE',
                                          'CAMPAIGN_NOTIFICATIONS:HARD_DELETE',
                                          'CAMPAIGN_NOTIFICATIONS:STATS',
    -- Contact
                                          'CONTACT:READ', 'CONTACT:UPDATE', 'CONTACT:SEARCH',
                                          'CONTACT:MARK_IN_PROGRESS',
                                          'CONTACT:MARK_RESOLVED', 'CONTACT:MARK_SPAM', 'CONTACT:DELETE',
                                          'CONTACT:STATS',
    -- Config
                                          'CONFIG:UPDATE_ALERT', 'CONFIG:TOGGLE_ALERT', 'CONFIG:UPDATE_APP_CONFIG'
    )
WHERE r.name = 'ADMIN';


-- USER — submit own applications, view own history, make payments
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.name IN (
                                          'USERS:CREATE', 'USERS:READ',
                                          'USERS:UPDATE', 'USERS:DELETE',
                                          'USERS:SEARCH', 'USERS:ENABLE',
                                          'USERS:DISABLE', 'USERS:CHANGE_PASSWORD', 'USERS:STATS'
    )
WHERE r.name = 'USER';
