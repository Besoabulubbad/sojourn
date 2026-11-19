# 0019 — Arrival reminders as local notifications

- **Status:** accepted
- **Date:** 2026-11-19
- **Deciders:** maintainer, with Claude Code
- **Tags:** bookings, platform, notifications, privacy

## Context and problem statement

From the product review (Phase 6): a guest has nothing that reminds them of an arrival. The maintainer chose a reminder the day before check-in. Sojourn has no backend that could send pushes, and the guest's bookings already live on the phone (ADR 0007).

## Decision drivers

- Nothing about the guest leaves the phone: no push service, no device token.
- The permission is asked only when the guest chooses to be reminded, with the reason on screen.
- The reminder must survive a restart and never show late.
- The words follow the app's language, which can differ from the phone's.

## Options considered

1. **Local notifications scheduled on the phone** — Android `AlarmManager` (inexact), iOS `UNUserNotificationCenter`.
2. **WorkManager on Android** — survives restarts by itself, but a new dependency for one alarm a booking.
3. **Push notifications** — need a server, an FCM/APNs setup and the guest's token; out of scope for a demo.

## Decision outcome

**Chosen: option 1.**

- **When:** `BookingRules.reminderAt`: 18:00 on the evening before check-in, on the phone's clock; none once that has passed or the booking is cancelled.
- **Where it is offered:** `SojournReminderCard` on the booking confirmation and on an upcoming booking's page. "Remind me" asks the system once; if notifications are already allowed, the reminder is scheduled without asking. A refusal shows where to turn them on.
- **Android:** `AndroidReminders` stores each reminder (so `RebootReceiver` re-arms it after a restart) and arms an inexact alarm, which needs no exact-alarm permission and may be a few minutes late. `ReminderReceiver` shows it on the "Arrival reminders" channel. Manifest: `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`. `MainActivity` binds the permission launcher.
- **iOS:** `IosReminders` uses a calendar trigger, which the system keeps across restarts.
- **Words:** written when scheduled, in the app's language, because nothing of the app runs when the notification shows.
- **Cancelling** a booking cancels its reminder.

### Consequences

- Good: no server, no tracking; the reminder works offline.
- Good: one small platform interface (`Reminders`), testable with fakes.
- Bad: a reminder keeps the words of the language it was scheduled in. Changing the app's language later does not reword it.
- Neutral: on Android the alarm may fire up to an hour after 18:00 (an inexact alarm's window).

### Confirmation

`BookingRulesTest` covers the time and "never late". `AndroidRemindersTest` (Robolectric) covers the alarm, the notification's words, re-arming after a restart, and cancelling. On the emulator: the permission dialog, "Reminder set", the alarm in `dumpsys alarm`, and cancellation removing it.

## Links

- Android: notification runtime permission; scheduling alarms (inexact `set`). iOS: `UNCalendarNotificationTrigger`.
- ADR 0007 (bookings on the phone); `docs/guides/accessibility.md` (the card announces its changes).
