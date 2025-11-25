# Earnzy Admin Panel

A professional, secured admin dashboard for managing an earning app platform with 50+ advanced features.

## Features

### 👥 User Management
- User list with search and filters
- Detailed user profile view
- User transaction history
- Manual & auto-ban system
- Referral tracking
- Leaderboard management

### 💰 Financial Management
- Coin control & adjustment
- Transaction tracking
- Withdrawal approval workflow
- Bonus & reward allocation
- Promo code management
- Revenue analytics

### 📦 Content Management
- Offer CRUD operations
- Banner management with image upload
- Offerwall & cards controller
- Task management
- Achievement system
- Android app UI control

### 💬 Communications
- FCM push notifications
- Notification templates
- Support ticket management
- Live chat with users
- Bulk notification campaigns

### 📊 Analytics & Monitoring
- Real-time dashboard stats
- User engagement metrics
- Revenue analytics
- Admin activity logs
- System health monitoring
- Export reports (CSV/PDF)

### 🔐 Security
- JWT authentication
- Role-based access control (RBAC)
- Rate limiting
- Audit logging
- Secure file uploads (S3 signed URLs)
- Data encryption

## Tech Stack

- **Backend**: Express.js with TypeScript
- **Database**: AWS DynamoDB (single-table design)
- **Storage**: AWS S3 for file uploads
- **Notifications**: Firebase Cloud Messaging
- **Frontend**: React + Vite + Tailwind CSS
- **Real-time**: WebSocket with Socket.IO
- **Infrastructure**: Terraform / CloudFormation

## Project Structure

```
earnzy-admin/
├── services/
│   ├── api/                 # Main Express API
│   ├── chat/                # WebSocket chat server
│   └── worker/              # Background jobs
├── web/                     # React admin dashboard
├── infra/                   # AWS infrastructure
├── scripts/                 # Deployment scripts
└── docs/                    # Architecture & security docs
```

## Quick Start

### Prerequisites
- Node.js 18+
- AWS Account with DynamoDB & S3 setup
- Firebase project for FCM

### Setup

1. **Clone repository**
```bash
git clone https://github.com/yourusername/earnzy-admin.git
cd earnzy-admin
```

2. **Install dependencies**
```bash
# API
cd services/api
npm install

# Frontend
cd ../../web
npm install
```

3. **Configure environment**
```bash
# services/api/.env
AWS_REGION=ap-south-1
AWS_ACCESS_KEY_ID=your_key
AWS_SECRET_ACCESS_KEY=your_secret
DYNAMODB_TABLE=earnzy-admin
S3_BUCKET=earnzy-uploads
FCM_PROJECT_ID=your_project_id
FCM_PRIVATE_KEY=your_private_key
JWT_SECRET=your_jwt_secret
API_PORT=3000

# web/.env
VITE_API_URL=http://localhost:3000
```

4. **Run locally**
```bash
# Terminal 1: Start API
cd services/api
npm run dev

# Terminal 2: Start Frontend
cd web
npm run dev
```

Visit http://localhost:5173 in your browser.

## API Endpoints

### Authentication
- `POST /api/auth/login` - Admin login
- `GET /api/auth/user` - Current admin info
- `POST /api/logout` - Logout

### Users
- `GET /api/users` - List all users
- `GET /api/users/:id` - User details
- `POST /api/users/:id/ban` - Ban user
- `POST /api/users/:id/unban` - Unban user

### Coins
- `POST /api/coins/adjust` - Adjust user coins
- `GET /api/coins/history/:userId` - Coin history

### Offers
- `GET /api/offers` - List offers
- `POST /api/offers` - Create offer
- `PATCH /api/offers/:id` - Update offer
- `DELETE /api/offers/:id` - Delete offer

### Withdrawals
- `GET /api/withdrawals` - List withdrawals
- `POST /api/withdrawals/:id/approve` - Approve
- `POST /api/withdrawals/:id/reject` - Reject

### Support
- `GET /api/support/tickets` - List tickets
- `POST /api/support/tickets/:id/messages` - Send message
- `POST /api/support/tickets/:id/status` - Update status

### Notifications
- `POST /api/notifications` - Send FCM notification
- `POST /api/notifications/bulk` - Bulk send

**See [docs/architecture.md](docs/architecture.md) for complete API documentation.**

## Security Features

✅ JWT authentication with 24h expiration
✅ Role-based access control (RBAC)
✅ Rate limiting (100 req/min)
✅ Audit logging for all admin actions
✅ Input validation with Zod
✅ OWASP security headers
✅ S3 signed URLs (time-limited)
✅ Password hashing with bcrypt
✅ Encrypted sensitive data

See [docs/security.md](docs/security.md) for detailed security implementation.

## Deployment

### Deploy to AWS

1. **Set up infrastructure**
```bash
cd infra/terraform
terraform init
terraform plan
terraform apply
```

2. **Deploy API**
```bash
cd services/api
npm run build
npm run deploy:prod
```

3. **Deploy Frontend**
```bash
cd web
npm run build
npm run deploy:prod
```

### Environment Variables for Production
Set these in AWS Secrets Manager:
- AWS_ACCESS_KEY_ID
- AWS_SECRET_ACCESS_KEY
- JWT_SECRET
- REFRESH_TOKEN_SECRET
- FCM_PRIVATE_KEY

## Testing

```bash
# Run unit tests
npm run test

# Run security tests
npm run test:security

# Check dependencies
npm audit
```

## Contributing

1. Create a feature branch: `git checkout -b feature/feature-name`
2. Commit changes: `git commit -m "Add feature"`
3. Push to branch: `git push origin feature/feature-name`
4. Open a Pull Request

## Documentation

- [Architecture Guide](docs/architecture.md) - System design & patterns
- [Security Guide](docs/security.md) - Security implementation
- [DynamoDB Design](docs/dynamodb-single-table-design.md) - Database schema

## Support

For issues, feature requests, or questions:
- Create an issue on GitHub
- Contact: support@earnzy.com

## License

MIT License - see LICENSE file for details

## Changelog

### v1.0.0 (2025-01-20)
- ✅ Initial release with 50+ features
- ✅ AWS DynamoDB single-table design
- ✅ FCM push notifications
- ✅ Live chat support
- ✅ Mobile-responsive UI
- ✅ Complete security implementation

---

**Made with ❤️ by Earnzy Team**
