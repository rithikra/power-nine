package com.example.powernine.deck;

import com.example.powernine.card.Card;
import com.example.powernine.deck.utils.DeckNotFoundException;
import com.example.powernine.deck.utils.DeckRatingException;
import com.example.powernine.user.User;
import com.example.powernine.user.UserRepository;
import com.example.powernine.user.utils.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
public class DeckController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private DeckRatingsRepository deckRatingsRepository;

    private static final Logger logger = LoggerFactory.getLogger(DeckController.class);

    @GetMapping("/decks")
    List<Deck> getDecks() {
        return deckRepository.findAll();
    }

    @PostMapping("/decks")
    Deck addDeck(@RequestBody Deck deck, Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("Unable to load principal user");
        User user = userRepository.findByUsername(principal.getName());
        deck.setUserUID(user.getUID());
        Deck newDeck = deckRepository.save(deck);
        if (!user.getDecks().contains(newDeck)) {
            user.getDecks().add(newDeck);
        }
        userRepository.save(user);
        return newDeck;
    }

    @GetMapping("/decks/{name}")
    Deck getDeckByName(@PathVariable String name) {
        Deck deck = deckRepository.findByDeckName(name);
        if (deck == null)
            throw new DeckNotFoundException("Deck name not found");
        return deck;
    }

    @GetMapping("/decks/{name}/{username}")
    Deck getUserDeckByName(@PathVariable String name, @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException(username);
        for (Deck deck: user.getDecks()) {
            if (deck.getDeckName().equals(name))
                return deck;
        }
        throw new DeckNotFoundException("Deck name for user not found");
    }

    @DeleteMapping("/decks/{name}")
    void deleteDeck(@PathVariable String name, Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("Unable to load principal user");
        User user = userRepository.findByUsername(principal.getName());
        Deck deck = user.getDeckByName(name);
        user.getDecks().remove(deck);
        deckRepository.delete(deck);
        userRepository.save(user);
    }

    @PutMapping("/decks/{name}")
    Deck addCardToDeck(@RequestBody Card card, @PathVariable String name, Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("Unable to load principal user");
        User user = userRepository.findByUsername(principal.getName());
        Deck deck = user.getDeckByName(name);
        deck.addCard(card);
        userRepository.save(user);
        return deckRepository.save(deck);
    }

    @DeleteMapping("/decks/delete-card/{name}")
    void removeCardFromDeck(@RequestBody Card card, @PathVariable String name, Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("Unable to load principal user");
        User user = userRepository.findByUsername(principal.getName());
        Deck deck = user.getDeckByName(name);
        deck.removeCard(card);
        userRepository.save(user);
        deckRepository.save(deck);
    }

    @PostMapping("/decks/rate/{name}/{uid}/{rate}")
    void rateDeck(@PathVariable String name, @PathVariable String uid, @PathVariable String rate, Principal principal) {
        if (principal == null)
            throw new UsernameNotFoundException("Unable to load principal user");
        Integer rating = Integer.parseInt(rate);
        Long userUID = Long.parseLong(uid);
        if (rating < 0 || rating > 5)
            throw new DeckRatingException("Unable to store value");
        User user = userRepository.findByUID(userUID);
        User userMakingRating = userRepository.findByUsername(principal.getName());
        if (user != null) {
            for (Deck deck: user.getDecks()) {
                if (deck.getDeckName().equals(name)) {
                    deckRatingsRepository.save(new DeckRatings(deck.getId(), userMakingRating.getUID(), rating));
                    return;
                }
            }
            throw new DeckNotFoundException(name);
        }
        throw new UsernameNotFoundException(String.valueOf(userUID));
    }

    @GetMapping("/decks/rate/{name}/{username}")
    List<DeckRatings> getDeckRatings(@PathVariable String name, @PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            for (Deck deck: user.getDecks()) {
                if (deck.getDeckName().equals(name))
                    return deckRatingsRepository.findAllByDeckUID(deck.getId());
            }
            throw new DeckNotFoundException(username);
        }
        throw new UsernameNotFoundException(username);
    }

}
