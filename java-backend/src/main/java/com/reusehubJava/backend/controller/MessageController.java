package com.reusehubJava.backend.controller;

import com.reusehubJava.backend.model.Message;
import com.reusehubJava.backend.model.User;
import com.reusehubJava.backend.repository.MessageRepository;
import com.reusehubJava.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176"})
public class MessageController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Send a new message
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request) {
        try {
            // Get current user from security context
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> senderOpt = userRepository.findByUCusMail(currentUserEmail);
            
            if (!senderOpt.isPresent()) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            // Find receiver
            Optional<User> receiverOpt = userRepository.findByUCusMail(request.getReceiverEmail());
            if (!receiverOpt.isPresent()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            Message message = new Message();
            message.setSender(senderOpt.get());
            message.setReceiver(receiverOpt.get());
            message.setContent(request.getContent());
            message.setSentDate(new Date());
            message.setIsRead(false);

            Message savedMessage = messageRepository.save(message);
            return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);

        } catch (Exception e) {
            System.out.println("Error sending message: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get conversation between current user and another user
    @GetMapping("/conversation/{otherUserEmail}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String otherUserEmail) {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> currentUserOpt = userRepository.findByUCusMail(currentUserEmail);
            Optional<User> otherUserOpt = userRepository.findByUCusMail(otherUserEmail);

            if (!currentUserOpt.isPresent() || !otherUserOpt.isPresent()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            List<Message> conversation = messageRepository.findConversationBetweenUsers(
                currentUserOpt.get(), otherUserOpt.get()
            );

            return new ResponseEntity<>(conversation, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error getting conversation: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all conversations for current user
    @GetMapping("/conversations")
    public ResponseEntity<List<Message>> getUserConversations() {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> currentUserOpt = userRepository.findByUCusMail(currentUserEmail);

            if (!currentUserOpt.isPresent()) {
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            User currentUser = currentUserOpt.get();
            List<Message> allMessages = messageRepository.findAllMessagesForUser(currentUser);
            
            // Process to get latest message from each conversation
            List<Message> conversations = new ArrayList<>();
            List<String> processedUsers = new ArrayList<>();
            
            for (Message message : allMessages) {
                String otherUserEmail;
                if (message.getSender().getUserId().equals(currentUser.getUserId())) {
                    otherUserEmail = message.getReceiver().getUCusMail();
                } else {
                    otherUserEmail = message.getSender().getUCusMail();
                }
                
                if (!processedUsers.contains(otherUserEmail)) {
                    conversations.add(message);
                    processedUsers.add(otherUserEmail);
                }
            }
            
            return new ResponseEntity<>(conversations, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error getting conversations: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Mark message as read
    @PutMapping("/read/{messageId}")
    public ResponseEntity<String> markAsRead(@PathVariable Long messageId) {
        try {
            Optional<Message> messageOpt = messageRepository.findById(messageId);
            if (!messageOpt.isPresent()) {
                return new ResponseEntity<>("Message not found", HttpStatus.NOT_FOUND);
            }

            Message message = messageOpt.get();
            message.setIsRead(true);
            messageRepository.save(message);

            return new ResponseEntity<>("Message marked as read", HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error marking message as read: " + e.getMessage());
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get unread message count
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> currentUserOpt = userRepository.findByUCusMail(currentUserEmail);

            if (!currentUserOpt.isPresent()) {
                return new ResponseEntity<>(0L, HttpStatus.UNAUTHORIZED);
            }

            Long unreadCount = messageRepository.countUnreadMessagesForUser(currentUserOpt.get());
            return new ResponseEntity<>(unreadCount, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("Error getting unread count: " + e.getMessage());
            return new ResponseEntity<>(0L, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DTO for message requests
    public static class MessageRequest {
        private String receiverEmail;
        private String content;

        // Getters and setters
        public String getReceiverEmail() { return receiverEmail; }
        public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
