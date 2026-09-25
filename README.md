# 🩺 OwnHabs

*Οι συνήθειές σου. Ο δικός σου ρυθμός. Τα δικά σου δεδομένα.*

Μια εφαρμογή παρακολούθησης συνηθειών για Android που έφτιαξα επειδή βαρέθηκα οι habit
trackers να θέλουν λογαριασμό, να με βομβαρδίζουν με ειδοποιήσεις να κάνω αναβάθμιση σε
premium, και να στέλνουν τα δεδομένα μου ο θεός ξέρει πού. Το OwnHabs δεν κάνει τίποτα
από αυτά. Καμία εγγραφή, κανένα cloud, καμία διαφήμιση. Ό,τι καταγράφεις μένει στο κινητό
σου, τελεία.
---

## Τι κάνει

- ✅ Συνήθειες τύπου **Ναι/Όχι** ή με **ποσοτικό στόχο** (π.χ. «Νερό — 2000 ml»)
- 🎯 Κατά την καταγραφή, επιλέγεις ρητά: **Έγινε πλήρως / Εν μέρει (με ακρίβεια μέσω slider ή πληκτρολογώντας την τιμή) / Δεν έγινε** — όχι σιωπηλά, μαντεμένα taps
- 🔁 Συχνότητα στα δικά σου μέτρα: καθημερινά, ή Χ φορές/εβδομάδα, ή Χ φορές/μήνα
  (π.χ. «καθαρισμός κατοικίδιου, 2 φορές την εβδομάδα»)
- 📅 Ημερολόγιο/heatmap 52 εβδομάδων, σαν το contribution graph του GitHub
- 🔥 Streaks — τρέχον και προσωπικό ρεκόρ
- ❄️ **Streak Freeze**: μία «κάρτα χάρης» τον μήνα ανά συνήθεια, καλύπτει μια χαμένη μέρα χωρίς να σπάσει το σερί σου
- 🏆 Μερικά απλά badges για κίνητρο (χωρίς gamification-εξάρτηση, μόνο ένα ελαφρύ nudge)
- 📁 Δικές σου κατηγορίες προτάσεων, πέρα από τις έτοιμες που έρχονται με την εφαρμογή
- 🏠 **Widget αρχικής οθόνης** — κατέγραψε τις 3 πρώτες συνήθειές σου χωρίς να ανοίξεις καν την εφαρμογή
- ⌚ **Wear OS companion** — μια δεύτερη, ανεξάρτητη εφαρμογή για το ρολόι σου. Δείχνει τις ενεργές συνήθειές σου και καταγράφεις με ένα tap απευθείας από τον καρπό σου, συγχρονισμένο με το τηλέφωνο μέσω Bluetooth (καμία σύνδεση cloud ούτε εκεί)
- 🚶 **Βήματα από το smartwatch στην Αρχική** — όσο είναι συνδεδεμένο το ρολόι, τα σημερινά σου βήματα εμφανίζονται σε μια μικρή κάρτα στην κορυφή της Αρχικής Οθόνης (διάβασμα μέσω Health Services, μεταφορά μέσω Bluetooth — καμία σύνδεση cloud)
- 🎯 **Αυτόματο «κλείσιμο» συνήθειας βημάτων** — αν έχεις μια ποσοτική συνήθεια με ενεργοποιημένο «Αυτόματο κλείσιμο από βήματα ρολογιού», μόλις τα βήματα του ρολογιού φτάσουν τον στόχο της ημέρας, η συνήθεια σημειώνεται μόνη της ως ολοκληρωμένη — χωρίς να πειράζει ποτέ κάτι που έχεις ήδη καταγράψει εσύ χειροκίνητα
- 🖼️ **Year in Pixels** — ένα ετήσιο, οπτικό recap της συνέπειάς σου (σαν το contribution graph του GitHub, αλλά για ολόκληρο τον χρόνο σου), με στατιστικά και δυνατότητα κοινοποίησης ως εικόνα
- 🔔 **Μηνιαία ειδοποίηση σύνοψης** — την 1η κάθε μήνα, μια τοπική ειδοποίηση με το πόσο συνεπής ήσουν, το καλύτερο σερί σου, και πόσα badges ξεκλείδωσες
- 🇬🇷 🇬🇧 **Δίγλωσση** (Ελληνικά/English) — εναλλαγή μέσα από τις Ρυθμίσεις, καμία εξάρτηση από τη γλώσσα συστήματος. Οι έτοιμες προτάσεις και τα badges μεταφράζονται αυτόματα· ό,τι γράφεις εσύ (τίτλοι συνηθειών, περιγραφές) μένει πάντα όπως το έγραψες, χωρίς καμία αυτόματη μετάφραση
- 💾 Export/import σε `.json` — τα δεδομένα σου, όποτε θες, όπου θες
- 📴 Καμία σύνδεση internet απαιτούμενη. Ποτέ — ούτε καν ο συγχρονισμός με το ρολόι, που γίνεται αποκλειστικά μέσω Bluetooth

## Roadmap (κάποια στιγμή, ίσως)

- [ ] Dark/light theme toggle (προς το παρόν είναι μόνο dark, γιατί έτσι το ήθελα)
- [ ] Καλύτερο στατιστικό ανά κατηγορία
- [ ] Πλήρης ποσοτική καταγραφή απευθείας από το widget και το ρολόι (προς το παρόν κάνουν μόνο toggle πλήρες/μηδέν — εξαίρεση οι συνήθειες βημάτων με auto-tracking, που κλείνουν αυτόματα)
- [ ] Wear OS tile / complication για ακόμα πιο γρήγορη πρόσβαση
- [ ] Κουμπί «Sync τώρα» για άμεση, εν απαιτήσει ενημέρωση βημάτων (προς το παρόν βασίζεται στο passive batching του Health Services, που μπορεί να καθυστερήσει λίγα λεπτά)

---

# 🇬🇧 English

*Your habits. Your pace. Your data.*

A habit-tracking app for Android I built because I was tired of habit trackers wanting
an account, nagging me to go premium, and shipping my data who-knows-where. OwnHabs
doesn't do any of that. No sign-up, no cloud, no ads. What you track stays on your phone,
period.

## What it does

- ✅ **Yes/No** or **numeric-goal** habits (e.g. "Water — 2000 ml")
- 🎯 Logging is a deliberate choice: **Fully done / Partially done (fine-tuned via slider or by typing the exact value) / Not done** — not silent, guessed taps
- 🔁 Frequency on your own terms: daily, or X times/week, or X times/month
  (e.g. "clean the pet, 2x a week")
- 📅 A 52-week heatmap, GitHub-contribution-graph style
- 🔥 Streaks — current and personal best
- ❄️ **Streak Freeze**: one "grace card" per habit per month, covers a missed day without breaking your streak
- 🏆 A handful of lightweight badges for motivation (no gamification rabbit hole, just a nudge)
- 📁 Your own custom suggestion categories, on top of the built-in ones
- 🏠 **Home screen widget** — log your top 3 habits without even opening the app
- ⌚ **Wear OS companion** — a second, standalone app for your watch. Shows your active habits and lets you log them with a tap right from your wrist, synced with the phone over Bluetooth (no cloud there either)
- 🚶 **Watch steps on the Home screen** — while your watch is connected, today's step count shows up in a small card at the top of Home (read via Health Services, transferred over Bluetooth — no cloud)
- 🎯 **Auto-completing step habits** — if a numeric habit has "Auto-complete from watch steps" turned on, it marks itself done the moment your watch reports you've hit today's goal — never overwrites anything you've already logged manually
- 🖼️ **Year in Pixels** — a visual, annual recap of your consistency (think GitHub's contribution graph, but for your whole year), with stats and a share-as-image button
- 🔔 **Monthly recap notification** — on the 1st of every month, a local notification summarizing your consistency, best streak, and badges earned
- 🇬🇷 🇬🇧 **Bilingual** (Greek/English) — switch in Settings, independent of system locale. Built-in suggestions and badges translate automatically; anything you type yourself (habit titles, descriptions) always stays exactly as written, with no automatic translation
- 💾 Export/import to `.json` — your data, whenever, wherever
- 📴 No internet connection required. Ever — not even watch syncing, which happens purely over Bluetooth

## Roadmap (someday, maybe)

- [ ] Dark/light theme toggle (dark-only for now, because that's what I wanted)
- [ ] Better per-category stats
- [ ] Full numeric logging straight from the widget and the watch (currently just toggles full/zero — step habits with auto-tracking are the exception, they close themselves automatically)
- [ ] Wear OS tile / complication for even faster access
- [ ] "Sync now" button for on-demand step updates (currently relies on Health Services' passive batching, which can lag a few minutes)

---

📄 License: MIT
