# 🩺 HabitPulse

Μια εφαρμογή παρακολούθησης συνηθειών για Android που έφτιαξα επειδή βαρέθηκα οι habit
trackers να θέλουν λογαριασμό, να με βομβαρδίζουν με ειδοποιήσεις να κάνω αναβάθμιση σε
premium, και να στέλνουν τα δεδομένα μου ο θεός ξέρει πού. Το HabitPulse δεν κάνει τίποτα
από αυτά. Καμία εγγραφή, κανένα cloud, καμία διαφήμιση. Ό,τι καταγράφεις μένει στο κινητό
σου, τελεία.

Δεν είναι έτοιμο για το Play Store ακόμα — είναι project σε εξέλιξη που δουλεύω στον
ελεύθερο χρόνο μου.

---

## Τι κάνει

- ✅ Συνήθειες τύπου **Ναι/Όχι** ή με **ποσοτικό στόχο** (π.χ. «Νερό — 2000 ml»)
- 🔁 Συχνότητα στα δικά σου μέτρα: καθημερινά, ή Χ φορές/εβδομάδα, ή Χ φορές/μήνα
  (π.χ. «καθαρισμός κατοικίδιου, 2 φορές την εβδομάδα»)
- 📅 Ημερολόγιο/heatmap 52 εβδομάδων, σαν το contribution graph του GitHub
- 🔥 Streaks — τρέχον και προσωπικό ρεκόρ
- 🏆 Μερικά απλά badges για κίνητρο (χωρίς gamification-εξάρτηση, μόνο ένα ελαφρύ nudge)
- 📁 Δικές σου κατηγορίες προτάσεων, πέρα από τις έτοιμες που έρχονται με την εφαρμογή
- 💾 Export/import σε `.json` — τα δεδομένα σου, όποτε θες, όπου θες
- 📴 Καμία σύνδεση internet απαιτούμενη. Ποτέ.

## Στοίβα

Kotlin, Jetpack Compose, Room (SQLite), αρχιτεκτονική MVVM. Χωρίς Firebase, χωρίς κανένα
analytics SDK, χωρίς κανένα SDK τρίτου γενικά.

## Πώς να το τρέξεις

```
git clone <this repo>
```
Άνοιξέ το με Android Studio (χρειάζεται αρκετά πρόσφατη έκδοση — δουλεύει με AGP 9.x),
άσε το Gradle sync να τελειώσει, Run σε emulator ή σε πραγματική συσκευή με Android 8+.

## Δομή

```
app/src/main/java/com/habitpulse/app/
├── data/           Room entities/DAOs, repositories, backup JSON
├── domain/         streaks, badge engine, υπολογισμός συχνότητας
├── ui/              Compose οθόνες (home, add/edit, history, badges, settings)
└── notifications/   τοπικές υπενθυμίσεις μέσω AlarmManager
```

## Roadmap (κάποια στιγμή, ίσως)

- [ ] Widgets στην αρχική οθόνη
- [ ] Dark/light theme toggle (προς το παρόν είναι μόνο dark, γιατί έτσι το ήθελα)
- [ ] Καλύτερο στατιστικό ανά κατηγορία

Pull requests / issues welcome, αν και είναι ακόμα πολύ προσωπικό project.

---

# 🇬🇧 English

A habit-tracking app for Android I built because I was tired of habit trackers wanting
an account, nagging me to go premium, and shipping my data who-knows-where. HabitPulse
doesn't do any of that. No sign-up, no cloud, no ads. What you track stays on your phone,
period.

Not on the Play Store yet — it's a work-in-progress side project.

## What it does

- ✅ **Yes/No** or **numeric-goal** habits (e.g. "Water — 2000 ml")
- 🔁 Frequency on your own terms: daily, or X times/week, or X times/month
  (e.g. "clean the pet, 2x a week")
- 📅 A 52-week heatmap, GitHub-contribution-graph style
- 🔥 Streaks — current and personal best
- 🏆 A handful of lightweight badges for motivation (no gamification rabbit hole, just a nudge)
- 📁 Your own custom suggestion categories, on top of the built-in ones
- 💾 Export/import to `.json` — your data, whenever, wherever
- 📴 No internet connection required. Ever.

## Stack

Kotlin, Jetpack Compose, Room (SQLite), MVVM. No Firebase, no analytics SDK, no third-party
SDKs at all.

## Running it

```
git clone <this repo>
```
Open in Android Studio (needs a fairly recent version — built against AGP 9.x), let Gradle
sync finish, hit Run on an emulator or a real device running Android 8+.

## Structure

```
app/src/main/java/com/habitpulse/app/
├── data/           Room entities/DAOs, repositories, JSON backup
├── domain/         streaks, badge engine, frequency math
├── ui/              Compose screens (home, add/edit, history, badges, settings)
└── notifications/   local reminders via AlarmManager
```

## Roadmap (someday, maybe)

- [ ] Home screen widgets
- [ ] Dark/light theme toggle (dark-only for now, because that's what I wanted)
- [ ] Better per-category stats

PRs / issues welcome, though it's still a pretty personal project.

---

📄 License: MIT (do whatever you want with it)
