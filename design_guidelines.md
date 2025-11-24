# Admin Panel Design Guidelines

## Design Approach: Modern SaaS Admin Dashboard
**Reference Inspiration:** Linear, Vercel Dashboard, Stripe Dashboard - optimized for data-dense, feature-rich admin interfaces with exceptional usability.

**Core Principles:**
- Information clarity over decoration
- Efficient scanning and task completion
- Professional, trustworthy aesthetic
- Mobile-responsive data visualization

---

## Typography System

**Font Family:** Inter (Google Fonts)
- **Headings:** 600 weight
  - Page titles: text-2xl (24px)
  - Section headers: text-lg (18px)
  - Card titles: text-base (16px)
- **Body:** 400 weight
  - Primary text: text-sm (14px)
  - Secondary/meta: text-xs (12px)
- **Data/Numbers:** 500 weight (for emphasis in tables and stats)

---

## Layout System

**Spacing Units:** Use Tailwind units of 2, 4, 6, and 8 consistently
- Component padding: p-4 to p-6
- Section gaps: gap-6 to gap-8
- Card spacing: space-y-4

**Grid Structure:**
- Sidebar: 16rem (256px) fixed width on desktop, collapsible hamburger on mobile
- Main content: Full remaining width with max-w-7xl container
- Dashboard cards: grid-cols-1 md:grid-cols-2 lg:grid-cols-4 for stat cards
- Data tables: Full-width within container with horizontal scroll on mobile

---

## Core Components

### Navigation
**Sidebar (Desktop):**
- Fixed left sidebar with sections: Dashboard, Users, Transactions, Offers, Notifications, Settings
- Group related features under collapsible sections (e.g., "User Management" contains Users, Bans, Achievements)
- Active state with subtle accent indication
- Icon + label for each menu item (Heroicons)

**Mobile Navigation:**
- Hamburger menu triggering slide-out drawer
- Bottom navigation bar for primary actions (Dashboard, Users, Notifications, Settings)

### Dashboard Overview
**Stats Cards:** 4-column grid on desktop, 1-column on mobile
- Large number display (text-3xl)
- Label and trend indicator (+12% this week)
- Small icon in corner
- Subtle border, no heavy shadows

**Charts Section:** 
- Line charts for trends (user growth, transaction volume)
- Bar charts for comparisons (daily earnings, offer performance)
- Pie/donut charts for distributions (user status breakdown)
- 2-column layout: Primary chart (col-span-2), secondary metrics (col-span-1)

### Data Tables
**Structure:**
- Sticky header row
- Alternating row backgrounds for readability
- Right-aligned numbers, left-aligned text
- Action buttons (view, edit, ban) in rightmost column
- Pagination at bottom with items-per-page selector
- Search bar and filters above table

**Mobile Adaptation:**
- Card-based view replacing table rows
- Most important info prominently displayed
- Expandable details on tap

### Cards & Panels
**Standard Card:**
- Subtle border (border border-gray-200)
- Rounded corners (rounded-lg)
- White background with minimal shadow
- Header with title and action button
- Content area with appropriate padding (p-6)

### Forms & Inputs
**Form Layout:**
- Two-column grid on desktop (grid-cols-2 gap-4)
- Single column on mobile
- Clear label above each input
- Helper text below when needed
- Required field indicators (*)

**Input Styling:**
- Border-based inputs (not filled background)
- Focus states with accent border
- Consistent height (h-10 for text inputs)
- Disabled state clearly visible

**Buttons:**
- Primary: Solid accent for main actions
- Secondary: Outline style for alternative actions
- Danger: Red for destructive actions (ban, delete)
- Icon buttons for table actions (square with icon)
- Size variants: Small (px-3 py-1.5), Medium (px-4 py-2), Large (px-6 py-3)

### Live Chat Interface
**Split-pane design:**
- Left: Ticket list with search/filter (w-80)
- Right: Active conversation view
- Message bubbles: User (left, light gray), Admin (right, accent)
- Fixed input bar at bottom
- Typing indicators and read receipts

### Image Upload Components
**Banner/Image Manager:**
- Drag-and-drop upload zone
- Thumbnail grid of uploaded images
- Preview modal on click
- Delete/replace actions
- Image optimization guidelines displayed

---

## Feature-Specific Layouts

**User Profile Details:** Modal/slide-over panel with tabs (Overview, Transactions, Activity Log, Settings)

**Transaction Management:** Table with advanced filters (date range, status, amount range) + export button

**Offer Card Manager:** Visual card grid showing live preview of offers with inline edit capabilities

**Notification Center:** Compose interface with user targeting options (all, specific segments, individual) + preview pane

**Promo Code Generator:** Form with code pattern builder + active codes table below

**Ban System:** Rules builder interface + banned users table with unban action

**Leaderboard Config:** Drag-and-drop ranking editor + live preview

**Withdrawal Approval:** Queue-style interface with approve/reject actions and amount verification

---

## Mobile Optimization

- Bottom sheet modals for forms and details
- Swipeable cards for quick actions
- Collapsible sections to reduce scroll depth
- Touch-friendly tap targets (min 44px)
- Sticky action buttons for critical workflows

---

## Data Visualization

**Chart Types:**
- Line charts: User growth trends, revenue over time
- Bar charts: Daily/weekly comparisons
- Donut charts: Category breakdowns (user types, transaction statuses)
- Use consistent accent for primary data series
- Grid lines for readability
- Responsive sizing with aspect-ratio constraints

---

## Responsive Breakpoints
- Mobile: < 768px (single column, bottom nav, card views)
- Tablet: 768px - 1024px (2-column grids, sidebar toggle)
- Desktop: > 1024px (full sidebar, multi-column layouts)

---

This design creates a professional, efficient admin experience optimized for managing complex data and executing administrative tasks quickly across all devices.