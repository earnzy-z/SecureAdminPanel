import { useQuery } from "@tanstack/react-query";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { MessageCircle, Clock } from "lucide-react";

export default function SupportTickets() {
  const { data: stats } = useQuery({
    queryKey: ["/api/admin/support/stats"],
    queryFn: () => fetch("/api/admin/support/stats").then(r => r.json()),
  });

  const { data: tickets } = useQuery({
    queryKey: ["/api/admin/support/tickets"],
    queryFn: () => fetch("/api/admin/support/tickets").then(r => r.json()),
  });

  return (
    <div className="space-y-6">
      {/* Stats */}
      <div className="grid grid-cols-4 gap-4">
        <Card className="p-4">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500">Total Tickets</p>
              <p className="text-2xl font-bold">{stats?.total || 0}</p>
            </div>
            <MessageCircle className="w-8 h-8 text-blue-500" />
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500">Open</p>
              <p className="text-2xl font-bold">{stats?.open || 0}</p>
            </div>
            <MessageCircle className="w-8 h-8 text-yellow-500" />
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500">In Progress</p>
              <p className="text-2xl font-bold">{stats?.inProgress || 0}</p>
            </div>
            <Clock className="w-8 h-8 text-purple-500" />
          </div>
        </Card>
        <Card className="p-4">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-sm text-gray-500">Closed</p>
              <p className="text-2xl font-bold">{stats?.closed || 0}</p>
            </div>
            <MessageCircle className="w-8 h-8 text-green-500" />
          </div>
        </Card>
      </div>

      {/* Tickets List */}
      <Card className="p-4">
        <h3 className="text-lg font-semibold mb-4">Active Support Tickets</h3>
        <div className="space-y-2">
          {tickets?.tickets?.map((ticket: any) => (
            <div
              key={ticket.id}
              className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
            >
              <div className="flex-1">
                <p className="font-semibold">{ticket.subject}</p>
                <p className="text-sm text-gray-500">User: {ticket.userId}</p>
              </div>
              <div className="flex items-center gap-3">
                <Badge variant={
                  ticket.status === "open" ? "destructive" :
                  ticket.status === "in_progress" ? "secondary" :
                  "default"
                }>
                  {ticket.status}
                </Badge>
                <Button size="sm" onClick={() => window.location.href = `/admin/support/${ticket.id}`}>
                  View Chat
                </Button>
              </div>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
