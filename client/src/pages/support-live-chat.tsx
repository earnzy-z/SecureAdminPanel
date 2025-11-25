import { useQuery, useMutation } from "@tanstack/react-query";
import { useEffect, useRef, useState } from "react";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Send } from "lucide-react";

export default function SupportLiveChat() {
  const [selectedTicket, setSelectedTicket] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const scrollRef = useRef<HTMLDivElement>(null);

  const { data: tickets } = useQuery({
    queryKey: ["/api/admin/support/tickets"],
    queryFn: () => fetch("/api/admin/support/tickets").then(r => r.json()),
  });

  const { data: ticketDetail } = useQuery({
    queryKey: ["/api/admin/support/tickets", selectedTicket],
    queryFn: () => fetch(`/api/admin/support/tickets/${selectedTicket}`).then(r => r.json()),
    enabled: !!selectedTicket,
  });

  const sendMessage = useMutation({
    mutationFn: (msg: string) =>
      apiRequest(`/api/admin/support/tickets/${selectedTicket}/message`, "POST", {
        adminId: "admin-001",
        message: msg,
      }),
    onSuccess: () => {
      setMessage("");
      queryClient.invalidateQueries({ queryKey: ["/api/admin/support/tickets", selectedTicket] });
    },
  });

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [ticketDetail?.messages]);

  return (
    <div className="grid grid-cols-3 gap-4 h-screen p-4">
      {/* Tickets List */}
      <div className="col-span-1 border rounded-lg overflow-hidden flex flex-col">
        <div className="p-4 border-b">
          <h2 className="text-lg font-bold">Support Tickets</h2>
        </div>
        <ScrollArea className="flex-1">
          <div className="space-y-2 p-4">
            {tickets?.tickets?.map((ticket: any) => (
              <Card
                key={ticket.id}
                className={`p-3 cursor-pointer ${selectedTicket === ticket.id ? "bg-primary text-white" : ""}`}
                onClick={() => setSelectedTicket(ticket.id)}
              >
                <div className="flex justify-between items-start gap-2">
                  <div className="flex-1">
                    <p className="font-semibold text-sm">{ticket.userId}</p>
                    <p className="text-xs opacity-75 line-clamp-2">{ticket.subject}</p>
                  </div>
                  <Badge variant="outline" className="text-xs">{ticket.status}</Badge>
                </div>
              </Card>
            ))}
          </div>
        </ScrollArea>
      </div>

      {/* Chat Area */}
      <div className="col-span-2 border rounded-lg overflow-hidden flex flex-col">
        {selectedTicket && ticketDetail ? (
          <>
            <div className="p-4 border-b">
              <div className="flex justify-between items-center">
                <div>
                  <h3 className="font-bold">{ticketDetail.ticket?.subject}</h3>
                  <p className="text-xs text-gray-500">{ticketDetail.ticket?.userId}</p>
                </div>
                <Badge>{ticketDetail.ticket?.status}</Badge>
              </div>
            </div>

            <ScrollArea ref={scrollRef} className="flex-1 p-4">
              <div className="space-y-4">
                {ticketDetail.messages?.map((msg: any, idx: number) => (
                  <div
                    key={idx}
                    className={`flex ${msg.senderType === "admin" ? "justify-end" : "justify-start"}`}
                  >
                    <div
                      className={`max-w-xs p-3 rounded-lg ${
                        msg.senderType === "admin"
                          ? "bg-primary text-white"
                          : "bg-gray-200 text-black"
                      }`}
                    >
                      <p className="text-sm">{msg.message}</p>
                      <p className="text-xs opacity-70 mt-1">
                        {new Date(msg.createdAt).toLocaleTimeString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </ScrollArea>

            <div className="p-4 border-t flex gap-2">
              <Input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Type a message..."
                onKeyDown={(e) => {
                  if (e.key === "Enter" && message.trim()) {
                    sendMessage.mutate(message);
                  }
                }}
              />
              <Button
                onClick={() => sendMessage.mutate(message)}
                disabled={!message.trim() || sendMessage.isPending}
              >
                <Send className="w-4 h-4" />
              </Button>
            </div>
          </>
        ) : (
          <div className="flex items-center justify-center h-full text-gray-500">
            Select a ticket to start chatting
          </div>
        )}
      </div>
    </div>
  );
}
