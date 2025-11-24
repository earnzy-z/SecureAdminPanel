import { useState, useEffect, useRef } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { SupportTicket, TicketMessage } from "@shared/schema";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useToast } from "@/hooks/use-toast";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { MessageSquare, Send, Clock, CheckCircle, XCircle } from "lucide-react";

export default function Support() {
  const [selectedTicketId, setSelectedTicketId] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const { toast } = useToast();

  const { data: tickets, isLoading } = useQuery<SupportTicket[]>({
    queryKey: ["/api/support/tickets"],
  });

  const { data: messages } = useQuery<TicketMessage[]>({
    queryKey: ["/api/support/tickets", selectedTicketId, "messages"],
    enabled: !!selectedTicketId,
  });

  const sendMessageMutation = useMutation({
    mutationFn: async (data: { ticketId: string; message: string }) => {
      return await apiRequest("POST", `/api/support/tickets/${data.ticketId}/messages`, {
        message: data.message,
        senderType: "admin",
        senderId: "admin",
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/support/tickets", selectedTicketId, "messages"] });
      queryClient.invalidateQueries({ queryKey: ["/api/support/tickets"] });
      setMessage("");
    },
  });

  const updateStatusMutation = useMutation({
    mutationFn: async ({ ticketId, status }: { ticketId: string; status: string }) => {
      return await apiRequest("POST", `/api/support/tickets/${ticketId}/status`, { status });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/support/tickets"] });
      toast({
        title: "Status Updated",
        description: "Ticket status has been updated successfully.",
      });
    },
  });

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const filteredTickets = tickets?.filter((ticket) =>
    statusFilter === "all" || ticket.status === statusFilter
  );

  const selectedTicket = tickets?.find((t) => t.id === selectedTicketId);

  const handleSendMessage = () => {
    if (message.trim() && selectedTicketId) {
      sendMessageMutation.mutate({ ticketId: selectedTicketId, message: message.trim() });
    }
  };

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { variant: "default" | "secondary" | "destructive"; color?: string }> = {
      open: { variant: "default", color: "bg-blue-600 hover:bg-blue-700" },
      in_progress: { variant: "default", color: "bg-yellow-600 hover:bg-yellow-700" },
      resolved: { variant: "default", color: "bg-green-600 hover:bg-green-700" },
      closed: { variant: "secondary" },
    };

    const config = variants[status] || { variant: "secondary" as const };
    return (
      <Badge variant={config.variant} className={config.color}>
        {status.replace("_", " ").charAt(0).toUpperCase() + status.replace("_", " ").slice(1)}
      </Badge>
    );
  };

  const getPriorityBadge = (priority: string) => {
    const colors: Record<string, string> = {
      low: "bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-200",
      medium: "bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-400",
      high: "bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-400",
    };

    return (
      <Badge variant="secondary" className={colors[priority]}>
        {priority.charAt(0).toUpperCase() + priority.slice(1)}
      </Badge>
    );
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-foreground">Support Tickets</h1>
          <p className="text-sm text-muted-foreground">Manage customer support conversations</p>
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-full md:w-48" data-testid="select-status-filter">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Status</SelectItem>
            <SelectItem value="open">Open</SelectItem>
            <SelectItem value="in_progress">In Progress</SelectItem>
            <SelectItem value="resolved">Resolved</SelectItem>
            <SelectItem value="closed">Closed</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle>Tickets</CardTitle>
          </CardHeader>
          <CardContent className="p-0">
            {isLoading ? (
              <div className="p-4 space-y-3">
                {[1, 2, 3, 4].map((i) => (
                  <Skeleton key={i} className="h-20" />
                ))}
              </div>
            ) : filteredTickets?.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 px-4">
                <MessageSquare className="h-12 w-12 mb-3 opacity-20" />
                <p className="text-sm text-muted-foreground text-center">No support tickets</p>
              </div>
            ) : (
              <ScrollArea className="h-[600px]">
                <div className="p-4 space-y-3">
                  {filteredTickets?.map((ticket) => (
                    <div
                      key={ticket.id}
                      onClick={() => setSelectedTicketId(ticket.id)}
                      className={`p-3 rounded-md border cursor-pointer hover-elevate ${
                        selectedTicketId === ticket.id ? "bg-muted border-primary" : ""
                      }`}
                      data-testid={`ticket-${ticket.id}`}
                    >
                      <div className="flex items-start justify-between gap-2 mb-2">
                        <h4 className="font-semibold text-sm line-clamp-1">{ticket.subject}</h4>
                        {getPriorityBadge(ticket.priority)}
                      </div>
                      <div className="flex items-center justify-between gap-2">
                        {getStatusBadge(ticket.status)}
                        <span className="text-xs text-muted-foreground">
                          {new Date(ticket.updatedAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>
                {selectedTicket ? selectedTicket.subject : "Select a ticket"}
              </CardTitle>
              {selectedTicket && (
                <Select
                  value={selectedTicket.status}
                  onValueChange={(status) =>
                    updateStatusMutation.mutate({ ticketId: selectedTicket.id, status })
                  }
                >
                  <SelectTrigger className="w-40" data-testid="select-ticket-status">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="open">Open</SelectItem>
                    <SelectItem value="in_progress">In Progress</SelectItem>
                    <SelectItem value="resolved">Resolved</SelectItem>
                    <SelectItem value="closed">Closed</SelectItem>
                  </SelectContent>
                </Select>
              )}
            </div>
          </CardHeader>
          <CardContent>
            {!selectedTicket ? (
              <div className="flex flex-col items-center justify-center py-24">
                <MessageSquare className="h-16 w-16 mb-4 opacity-20" />
                <p className="text-muted-foreground">Select a ticket to view conversation</p>
              </div>
            ) : (
              <div className="space-y-4">
                <ScrollArea className="h-[400px] pr-4">
                  <div className="space-y-4">
                    {messages?.length === 0 ? (
                      <p className="text-sm text-muted-foreground text-center py-8">
                        No messages yet
                      </p>
                    ) : (
                      messages?.map((msg) => (
                        <div
                          key={msg.id}
                          className={`flex ${msg.senderType === "admin" ? "justify-end" : "justify-start"}`}
                        >
                          <div
                            className={`max-w-[80%] rounded-lg p-3 ${
                              msg.senderType === "admin"
                                ? "bg-primary text-primary-foreground"
                                : "bg-muted"
                            }`}
                          >
                            <p className="text-sm">{msg.message}</p>
                            <p className={`text-xs mt-2 ${
                              msg.senderType === "admin" ? "text-primary-foreground/70" : "text-muted-foreground"
                            }`}>
                              {new Date(msg.createdAt).toLocaleString()}
                            </p>
                          </div>
                        </div>
                      ))
                    )}
                    <div ref={messagesEndRef} />
                  </div>
                </ScrollArea>

                <div className="flex gap-2 pt-4 border-t">
                  <Input
                    placeholder="Type your message..."
                    value={message}
                    onChange={(e) => setMessage(e.target.value)}
                    onKeyPress={(e) => e.key === "Enter" && handleSendMessage()}
                    data-testid="input-message"
                  />
                  <Button
                    onClick={handleSendMessage}
                    disabled={!message.trim() || sendMessageMutation.isPending}
                    data-testid="button-send"
                  >
                    <Send className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
