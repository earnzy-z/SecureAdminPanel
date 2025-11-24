# Admin Panel - Earning App

## Overview

This is a comprehensive admin panel for managing an earning app platform built with a modern tech stack. The application allows administrators to manage users, transactions, offers, achievements, support tickets, withdrawals, and various other platform features through a clean, professional dashboard interface.

The frontend is built with React, TypeScript, and Vite, using shadcn/ui components with Tailwind CSS for styling. The backend uses Express.js with a PostgreSQL database accessed through Drizzle ORM. The application follows a monorepo structure with shared type definitions between client and server.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Frontend Architecture

**Technology Stack:**
- React 18 with TypeScript for type safety
- Vite as the build tool and development server
- Wouter for lightweight client-side routing
- TanStack Query (React Query) for server state management
- shadcn/ui component library built on Radix UI primitives
- Tailwind CSS for utility-first styling with custom design tokens
- React Hook Form with Zod for form validation

**Design System:**
- Follows a modern SaaS admin dashboard aesthetic inspired by Linear, Vercel, and Stripe
- Uses Inter font family from Google Fonts
- Implements a comprehensive theming system supporting light and dark modes
- Custom CSS variables for colors, spacing, shadows, and elevations
- Responsive design with mobile-first approach using Tailwind breakpoints

**Component Structure:**
- Modular page-based components in `client/src/pages/`
- Reusable UI components from shadcn/ui in `client/src/components/ui/`
- Custom components like `StatCard`, `AppSidebar`, and theme providers
- Sidebar navigation with collapsible sections for different admin features

### Backend Architecture

**Technology Stack:**
- Express.js web server
- TypeScript for type safety across the stack
- Drizzle ORM for database operations
- Neon serverless PostgreSQL with WebSocket support
- Session-based architecture (connect-pg-simple for session storage)

**API Design:**
- RESTful API endpoints following resource-based URL patterns
- Consistent response formats with JSON
- Request/response logging middleware for debugging
- Error handling with appropriate HTTP status codes

**Server Configuration:**
- Separate entry points for development (`index-dev.ts`) and production (`index-prod.ts`)
- Development mode uses Vite middleware for HMR and SSR of React app
- Production mode serves pre-built static files from `dist/public`
- Custom logging system with timestamps for request tracking

### Database Architecture

**ORM & Schema:**
- Drizzle ORM with PostgreSQL dialect
- Schema-first approach with TypeScript types generated from database schema
- Zod schemas generated from Drizzle schemas for validation
- Migration support through Drizzle Kit

**Core Data Models:**
- **Users:** Account management with coins, referrals, ban status
- **Transactions:** Financial activity tracking (earn, spend, bonus, referral, withdrawal)
- **Offers:** Promotional offers with categories, rewards, and active status
- **Banners:** Marketing banners with priority ordering
- **Tasks:** User tasks with coin rewards and completion tracking
- **Achievements:** Gamification system with user progress
- **Promo Codes:** Discount/reward codes with usage limits
- **Support Tickets:** Customer support with threaded messages
- **Withdrawals:** Payment processing with approval workflow
- **Referrals:** Referral tracking and rewards
- **Leaderboard:** User ranking system
- **Notifications:** Push notification management
- **Auto-Ban Rules:** Automated user moderation

**Storage Layer:**
- Abstract storage interface (`IStorage`) defining all data operations
- Supports CRUD operations for all entities
- Specialized operations like ban/unban, coin adjustments, bulk operations
- Transaction support for atomic operations

### Build & Deployment

**Development Workflow:**
- TypeScript compilation with incremental builds
- Vite dev server with HMR for instant feedback
- Path aliases for clean imports (`@/`, `@shared/`, `@assets/`)
- Type checking without emit (`noEmit: true`)

**Production Build:**
- Vite builds optimized client bundle to `dist/public`
- esbuild bundles server code to `dist/index.js`
- ES modules throughout (type: "module" in package.json)
- Static asset serving with fallback to index.html for client-side routing

**Project Structure:**
- `/client` - React frontend application
- `/server` - Express backend server
- `/shared` - Shared TypeScript types and schemas
- Monorepo approach with shared dependencies

## External Dependencies

### Database
- **Neon PostgreSQL:** Serverless PostgreSQL database with WebSocket support for connection pooling
- **Drizzle ORM:** TypeScript ORM with schema migrations and type-safe queries
- **connect-pg-simple:** PostgreSQL session store for Express sessions

### UI Framework
- **shadcn/ui:** Component library built on Radix UI primitives
- **Radix UI:** Headless UI components (30+ primitives for dialogs, dropdowns, tooltips, etc.)
- **Tailwind CSS:** Utility-first CSS framework with custom configuration
- **Lucide React:** Icon library for consistent iconography

### State Management & Data Fetching
- **TanStack Query (React Query):** Server state management with caching, background updates, and optimistic updates
- **React Hook Form:** Form state management with performance optimization
- **Zod:** Runtime type validation and schema validation

### Charting & Visualization
- **Recharts:** React charting library for dashboard visualizations (bar charts, line charts, pie charts)

### Development Tools
- **Vite:** Fast build tool with HMR and optimized production builds
- **TypeScript:** Type safety and developer experience
- **Replit Plugins:** Development environment integrations (cartographer, dev banner, runtime error modal)

### Fonts
- **Google Fonts (Inter):** Primary font family loaded from CDN for optimal performance

### Utilities
- **clsx & tailwind-merge:** Utility for combining Tailwind classes
- **class-variance-authority:** Type-safe variant styling system
- **date-fns:** Date manipulation and formatting
- **nanoid:** Unique ID generation