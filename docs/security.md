# Security Implementation Guide

## Authentication

### JWT Strategy
- **Algorithm**: HS256 (HMAC with SHA-256)
- **Secret**: Stored in AWS Secrets Manager
- **Expiration**: 24 hours
- **Refresh Token**: Rotated on each refresh (stored in DynamoDB with 7-day expiration)

### Password Hashing
- **Algorithm**: bcrypt with salt rounds 10
- **Never stored in plain text**: All passwords hashed before storage
- **Verification**: Use bcrypt.compare() for authentication

### Session Management
- Sessions stored in DynamoDB
- Session data includes: admin ID, role, permissions, IP address
- Session invalidation on logout
- Concurrent session prevention (one session per admin)

## Authorization (RBAC)

### Permission Matrix

| Resource | Super Admin | Admin | Moderator |
|----------|-----------|-------|-----------|
| Users | ✅ | ✅ | ✅ |
| Ban Users | ✅ | ✅ | ✅ |
| Coins | ✅ | ❌ | ❌ |
| Transactions | ✅ | ✅ | ❌ |
| Withdrawals | ✅ | ✅ | ❌ |
| Offers | ✅ | ✅ | ❌ |
| Settings | ✅ | ❌ | ❌ |

### Implementation
```typescript
// Route protection
app.post("/api/coins/adjust", 
  authMiddleware,
  rbacMiddleware(["SUPER_ADMIN"]),
  coinsController.adjust
);
```

## Input Validation

### Validation Strategy
1. **Schema Validation**: Zod schemas for all inputs
2. **Type Checking**: TypeScript strict mode
3. **Sanitization**: Remove HTML/script tags from strings

### Example
```typescript
const adjustCoinsSchema = z.object({
  userId: z.string().uuid(),
  amount: z.number().int().min(-10000).max(10000),
  description: z.string().max(500)
});

const data = adjustCoinsSchema.parse(req.body);
```

## API Security

### CORS Configuration
```typescript
app.use(cors({
  origin: process.env.ADMIN_URL,
  credentials: true,
  maxAge: 86400
}));
```

### Rate Limiting
- Global: 100 requests/minute per IP
- Per-endpoint: 10 requests/minute for sensitive operations
- Implemented using express-rate-limit

### OWASP Headers
```typescript
app.use(helmet()); // Sets security headers
```

## Data Protection

### Encryption at Rest
- S3: Server-side encryption (AES-256)
- DynamoDB: Point-in-time recovery with encryption
- Sensitive fields: Encrypted before storage using AES-256-GCM

### Encryption in Transit
- HTTPS/TLS 1.3 only
- S3 presigned URLs have 1-hour expiration
- API endpoints require HTTPS

### Data Minimization
- Only collect necessary user data
- Implement data retention policies (delete old logs after 90 days)
- Support for GDPR data deletion requests

## Audit Logging

### What to Log
- ✅ Admin login/logout
- ✅ User bans/unbans
- ✅ Coin adjustments
- ✅ Withdrawal approvals
- ✅ Offer management
- ✅ Failed login attempts
- ✅ Permission denied actions

### Audit Log Schema
```json
{
  "PK": "AUDIT_LOG#admin-001",
  "SK": "2025-01-20T10:30:00Z#event-123",
  "adminId": "admin-001",
  "action": "USER_BAN",
  "resourceId": "user-123",
  "status": "SUCCESS",
  "details": { /* action details */ },
  "ipAddress": "192.168.1.1",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2025-01-20T10:30:00Z"
}
```

## Third-Party Integrations

### Firebase Cloud Messaging
- Service account key stored in AWS Secrets Manager
- Accessed via environment variable, never hardcoded
- Scoped permissions: Only send notifications, no data access

### AWS Services
- **DynamoDB**: IAM role with least privilege (only table access)
- **S3**: Presigned URLs for client uploads (never direct access)
- **CloudWatch**: Only admin account has access

## Secrets Management

### Secret Rotation
- JWT_SECRET: Rotate every 90 days
- Database password: Rotate every 30 days
- API keys: Rotate every 6 months

### Storage
- AWS Secrets Manager for production
- .env.local (gitignored) for development
- Never commit secrets to repository

## Vulnerability Management

### Dependencies
- Run `npm audit` before each deployment
- Update vulnerable packages immediately
- Security monitoring with Snyk (optional)

### Code Review
- All code changes reviewed for security
- Manual penetration testing quarterly
- Automated security scanning with SonarQube

## Incident Response

### Security Incident Procedure
1. **Detect**: CloudWatch alarms for suspicious activity
2. **Contain**: Disable affected admin account
3. **Investigate**: Review audit logs
4. **Remediate**: Apply fixes and deploy
5. **Document**: Record in incident log

### Brute Force Protection
- Lock account after 5 failed login attempts
- 15-minute lockout period
- Admin notification on suspicious activity

## Testing

### Security Testing Checklist
- [ ] Authentication bypass attempts
- [ ] Authorization bypass attempts
- [ ] SQL injection attempts (N/A with DynamoDB)
- [ ] XSS payload injection
- [ ] CSRF token validation
- [ ] Rate limiting effectiveness
- [ ] Sensitive data exposure
- [ ] Weak cryptography

### Automated Testing
```bash
npm run test:security    # Run security tests
npm run audit           # Check dependencies
```
