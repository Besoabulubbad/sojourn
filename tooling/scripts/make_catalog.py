#!/usr/bin/env python3
"""
Generates the seed catalogue: 40 stays across London, Paris, Lisbon, Algiers, Constantine and Oran (ADR 0007).

Why a script: the catalogue is data the app ships and the docs site serves, so it must be reproducible and
reviewable. Titles and descriptions are written here by hand in each host's own language (S-06); facts and nightly
prices match each title. Secondary values (ratings, amenities, policies, blocked dates, photo picks) come from a
seeded random generator, so every run produces the same file.

Inputs:  tooling/catalog/photo-pool.json   the hand-curated CC0 photos (ADR 0016) and their room categories
Outputs: core/network/src/commonMain/catalog/catalog.json   embedded in the app (sojourn.catalog plugin)
         docs/catalog/catalog.json                          served by the docs site for KtorSojournApi

Usage:
    python3 -I tooling/scripts/make_catalog.py
"""
from __future__ import annotations

import json
import random
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
POOL = ROOT / "tooling/catalog/photo-pool.json"
OUTPUTS = [ROOT / "core/network/src/commonMain/catalog/catalog.json", ROOT / "docs/catalog/catalog.json"]
SEED = 20261117

# id, country, currency, centre (lat, lng), photo category, cleaning fee range (minor), city tax per night (minor)
CITIES = [
    ("london", "GB", "GBP", (51.5074, -0.1278), (3500, 7500), 0),
    ("paris", "FR", "EUR", (48.8566, 2.3522), (4000, 8000), 325),
    ("lisbon", "PT", "EUR", (38.7223, -9.1393), (3000, 6000), 400),
    ("algiers", "DZ", "DZD", (36.7538, 3.0588), (150000, 300000), 0),
    ("constantine", "DZ", "DZD", (36.3650, 6.6147), (120000, 250000), 0),
    ("oran", "DZ", "DZD", (35.6971, -0.6308), (130000, 260000), 0),
]

AREAS = {
    "area_paddington": (51.5154, -0.1755), "area_shoreditch": (51.5265, -0.0786),
    "area_notting_hill": (51.5098, -0.2056), "area_camden": (51.5390, -0.1426),
    "area_south_bank": (51.5055, -0.1160), "area_kings_cross": (51.5308, -0.1238),
    "area_le_marais": (48.8590, 2.3620), "area_montmartre": (48.8867, 2.3431),
    "area_saint_germain": (48.8530, 2.3330), "area_canal_saint_martin": (48.8710, 2.3650),
    "area_bastille": (48.8532, 2.3690),
    "area_alfama": (38.7110, -9.1300), "area_chiado": (38.7105, -9.1420), "area_principe_real": (38.7170, -9.1490),
    "area_belem": (38.6970, -9.2060), "area_graca": (38.7170, -9.1310),
    "area_hydra": (36.7400, 3.0330), "area_el_biar": (36.7690, 3.0290), "area_bab_el_oued": (36.7900, 3.0500),
    "area_sidi_yahia": (36.7380, 3.0210),
    "area_sidi_mabrouk": (36.3650, 6.6300), "area_constantine_centre": (36.3650, 6.6100),
    "area_el_kantara": (36.3720, 6.6150),
    "area_front_de_mer": (35.7020, -0.6450), "area_sidi_el_houari": (35.7080, -0.6560),
    "area_les_falaises": (35.7010, -0.6270),
}

# city, area, language, title, description, bedrooms, beds, bathrooms, sleeps, nightly (major units)
STAYS = [
    ("london", "area_paddington", "en", "Bright studio by the canal",
     "A calm, light-filled studio two minutes from the canal, with a proper desk and fast Wi-Fi.", 0, 1, 1, 2, 92),
    ("london", "area_shoreditch", "en", "Warehouse loft off Redchurch Street",
     "Exposed brick, tall windows and a long oak table. Galleries and coffee on the doorstep.", 1, 1, 1, 2, 148),
    ("london", "area_notting_hill", "en", "Garden flat near Portobello Road",
     "A quiet ground-floor flat with its own small garden, a short walk from the market.", 2, 2, 1, 4, 185),
    ("london", "area_camden", "en", "Top-floor one-bed with rooftop views",
     "Bright and simple, with views over Camden's rooftops and the canal path below.", 1, 1, 1, 2, 118),
    ("london", "area_south_bank", "en", "Riverside two-bed for longer stays",
     "Two bedrooms, two baths and a washer-dryer, ten minutes' walk from the river.", 2, 2, 2, 4, 210),
    ("london", "area_kings_cross", "en", "Compact studio steps from King's Cross",
     "Everything you need in a well-planned space, with trains to Paris around the corner.", 0, 1, 1, 2, 98),
    ("london", "area_paddington", "en", "Family house with a sunny kitchen",
     "Three bedrooms over two floors and a big kitchen that gets the afternoon sun.", 3, 4, 2, 6, 265),
    ("london", "area_shoreditch", "en", "Quiet one-bed with a home office",
     "A separate office with a door that closes, so work and evenings stay apart.", 1, 1, 1, 2, 135),
    ("paris", "area_le_marais", "fr", "Studio lumineux au cœur du Marais",
     "Un studio clair sous les toits, à deux pas de la place des Vosges.", 0, 1, 1, 2, 110),
    ("paris", "area_montmartre", "fr", "Deux-pièces avec vue sur les toits",
     "Un appartement calme sur la butte, avec un balcon qui regarde tout Paris.", 1, 1, 1, 3, 145),
    ("paris", "area_saint_germain", "fr", "Appartement de caractère à Saint-Germain",
     "Parquet, moulures et cheminée, entre les librairies et le jardin du Luxembourg.", 2, 2, 1, 4, 235),
    ("paris", "area_canal_saint_martin", "fr", "Loft au bord du canal Saint-Martin",
     "Un grand volume lumineux, idéal pour travailler, au calme d'une cour pavée.", 1, 2, 1, 3, 160),
    ("paris", "area_bastille", "fr", "Trois-pièces familial près de Bastille",
     "Deux chambres, une cuisine équipée et un marché le dimanche au bout de la rue.", 2, 3, 1, 5, 190),
    ("paris", "area_le_marais", "fr", "Petit nid calme pour un long séjour",
     "Pensé pour les séjours d'un mois : bureau, lave-linge et lumière du matin.", 1, 1, 1, 2, 125),
    ("paris", "area_montmartre", "fr", "Atelier d'artiste avec verrière",
     "Une verrière plein nord, des murs blancs et un escalier en colimaçon.", 1, 1, 1, 2, 170),
    ("paris", "area_saint_germain", "fr", "Studio élégant près du Bon Marché",
     "Un studio soigné, bien rangé et silencieux, dans un immeuble haussmannien.", 0, 1, 1, 2, 135),
    ("lisbon", "area_alfama", "pt", "Apartamento com vista para o Tejo",
     "Janelas sobre o rio e os telhados de Alfama, a dois passos do elétrico 28.", 1, 1, 1, 2, 95),
    ("lisbon", "area_chiado", "pt", "Estúdio luminoso no Chiado",
     "Um estúdio claro e tranquilo, entre livrarias, cafés e o miradouro.", 0, 1, 1, 2, 85),
    ("lisbon", "area_principe_real", "pt", "T2 com varanda no Príncipe Real",
     "Dois quartos, uma varanda com sol de tarde e o jardim ao fundo da rua.", 2, 2, 1, 4, 140),
    ("lisbon", "area_belem", "pt", "Casa familiar perto dos Jerónimos",
     "Espaço para a família toda, cozinha grande e o rio a dez minutos a pé.", 3, 3, 2, 6, 150),
    ("lisbon", "area_graca", "pt", "T1 sossegado na Graça",
     "Um apartamento calmo com secretária, ideal para trabalhar à distância.", 1, 1, 1, 2, 78),
    ("lisbon", "area_alfama", "pt", "Casa antiga recuperada em Alfama",
     "Paredes de pedra, azulejos originais e um pátio pequeno e fresco.", 2, 2, 1, 4, 120),
    ("lisbon", "area_chiado", "pt", "Loft moderno para estadias longas",
     "Pensado para meses: máquina de lavar, boa luz e internet rápida.", 1, 1, 1, 2, 110),
    ("algiers", "area_hydra", "ar", "شقة مشرقة في حيدرة",
     "شقة هادئة مليئة بالضوء على بعد دقائق من الحديقة، مع مكتب وإنترنت سريع.", 2, 3, 2, 5, 12000),
    ("algiers", "area_el_biar", "fr", "Appartement familial à El Biar",
     "Trois chambres, un grand salon et une terrasse avec vue sur la baie.", 3, 4, 2, 6, 15500),
    ("algiers", "area_bab_el_oued", "ar", "استوديو قريب من البحر",
     "استوديو صغير ومرتب، على بعد خطوات من الكورنيش.", 0, 1, 1, 2, 7000),
    ("algiers", "area_sidi_yahia", "fr", "Studio moderne à Sidi Yahia",
     "Un studio neuf et bien équipé, au calme, près des cafés et des commerces.", 0, 1, 1, 2, 8500),
    ("algiers", "area_hydra", "en", "Quiet flat for a long stay in Hydra",
     "Two bedrooms, a washer and a proper desk, in a calm street close to the embassies.", 2, 2, 1, 4, 13000),
    ("algiers", "area_el_biar", "ar", "شقة واسعة مع شرفة",
     "شقة واسعة بثلاث غرف وشرفة كبيرة تطل على المدينة.", 3, 3, 2, 6, 14500),
    ("algiers", "area_bab_el_oued", "fr", "Deux-pièces lumineux près de la mer",
     "Un appartement clair à deux pas de la mer, idéal pour un mois au soleil.", 1, 1, 1, 3, 9000),
    ("constantine", "area_constantine_centre", "fr", "Appartement près des ponts suspendus",
     "Un appartement calme en plein centre, à quelques minutes du pont Sidi M'Cid.", 2, 2, 1, 4, 9500),
    ("constantine", "area_sidi_mabrouk", "ar", "شقة عائلية في سيدي مبروك",
     "شقة عائلية مريحة بثلاث غرف، قريبة من المحلات والمدارس.", 3, 3, 1, 6, 10500),
    ("constantine", "area_el_kantara", "ar", "استوديو بإطلالة على الوادي",
     "استوديو هادئ يطل على وادي الرمال، مثالي للإقامات الطويلة.", 0, 1, 1, 2, 5500),
    ("constantine", "area_constantine_centre", "fr", "Studio pratique pour un long séjour",
     "Tout le nécessaire pour un mois de travail : bureau, lave-linge et fibre.", 0, 1, 1, 2, 6000),
    ("constantine", "area_sidi_mabrouk", "fr", "Deux-pièces calme avec balcon",
     "Une chambre, un salon lumineux et un balcon au soleil du matin.", 1, 1, 1, 3, 7500),
    ("oran", "area_front_de_mer", "fr", "Appartement face à la mer",
     "De grandes fenêtres sur la Méditerranée et le front de mer à vos pieds.", 2, 2, 1, 4, 11500),
    ("oran", "area_sidi_el_houari", "ar", "بيت قديم مرمم في سيدي الهواري",
     "بيت تقليدي مرمم بعناية في قلب المدينة القديمة.", 2, 3, 1, 5, 9000),
    ("oran", "area_les_falaises", "fr", "Studio lumineux aux Falaises",
     "Un studio moderne avec vue dégagée, au calme, près de la corniche.", 0, 1, 1, 2, 6500),
    ("oran", "area_front_de_mer", "en", "Sea-view one-bed for remote work",
     "A desk by the window, fast internet and the sea in front of you all day.", 1, 1, 1, 2, 8500),
    ("oran", "area_les_falaises", "ar", "شقة واسعة للعائلات",
     "شقة بثلاث غرف وصالون كبير، قريبة من البحر والحدائق.", 3, 4, 2, 7, 12500),
]

# Each description in the app's languages, as the server's one-off translation (the Details "Translate" action).
# Keyed by stay position in STAYS; the host's own language is never repeated.
TRANSLATIONS = [
    {"ar": "استوديو هادئ ومشرق على بعد دقيقتين من القناة، مع مكتب حقيقي وإنترنت سريع."},
    {"ar": "جدران من الطوب ونوافذ عالية وطاولة طويلة من خشب البلوط. المعارض والمقاهي عند الباب."},
    {"ar": "شقة هادئة في الطابق الأرضي مع حديقة صغيرة خاصة، على بعد خطوات من السوق."},
    {"ar": "شقة مشرقة وبسيطة تطل على أسطح كامدن وممر القناة في الأسفل."},
    {"ar": "غرفتا نوم وحمامان وغسالة ونشافة، على بعد عشر دقائق سيرًا من النهر."},
    {"ar": "كل ما تحتاجه في مساحة مدروسة، والقطارات إلى باريس عند الزاوية."},
    {"ar": "ثلاث غرف نوم على طابقين ومطبخ كبير تدخله شمس العصر."},
    {"ar": "مكتب منفصل بباب يُغلق، فيبقى العمل بعيدًا عن المساء."},
    {"en": "A bright studio under the eaves, a short walk from the Place des Vosges.",
     "ar": "استوديو مشرق تحت السقف، على بعد خطوات من ساحة فوج."},
    {"en": "A quiet flat on the hill, with a balcony that looks out over all of Paris.",
     "ar": "شقة هادئة على التلة، مع شرفة تطل على باريس كلها."},
    {"en": "Parquet floors, cornicing and a fireplace, between the bookshops and the Luxembourg Gardens.",
     "ar": "أرضيات خشبية وزخارف وموقد، بين المكتبات وحديقة لوكسمبورغ."},
    {"en": "A large, bright space made for working, in a quiet cobbled courtyard.",
     "ar": "مساحة واسعة ومشرقة مثالية للعمل، في فناء مرصوف هادئ."},
    {"en": "Two bedrooms, a fitted kitchen and a Sunday market at the end of the street.",
     "ar": "غرفتا نوم ومطبخ مجهز وسوق يوم الأحد في آخر الشارع."},
    {"en": "Made for month-long stays: a desk, a washer and morning light.",
     "ar": "مصممة للإقامات الشهرية: مكتب وغسالة وضوء الصباح."},
    {"en": "A north-facing glass roof, white walls and a spiral staircase.",
     "ar": "سقف زجاجي نحو الشمال وجدران بيضاء ودرج حلزوني."},
    {"en": "A neat, quiet, well-kept studio in a Haussmann building.",
     "ar": "استوديو أنيق ومرتب وهادئ في مبنى على طراز أوسمان."},
    {"en": "Windows over the river and the rooftops of Alfama, steps from the number 28 tram.",
     "ar": "نوافذ تطل على النهر وأسطح ألفاما، على بعد خطوات من الترام 28."},
    {"en": "A bright, quiet studio among bookshops, cafés and the viewpoint.",
     "ar": "استوديو مشرق وهادئ بين المكتبات والمقاهي والمطل."},
    {"en": "Two bedrooms, a balcony with afternoon sun and the garden at the end of the street.",
     "ar": "غرفتا نوم وشرفة تدخلها شمس العصر والحديقة في آخر الشارع."},
    {"en": "Room for the whole family, a big kitchen and the river a ten-minute walk away.",
     "ar": "مساحة للعائلة كلها ومطبخ كبير والنهر على بعد عشر دقائق سيرًا."},
    {"en": "A calm flat with a desk, made for working remotely.",
     "ar": "شقة هادئة مع مكتب، مثالية للعمل عن بُعد."},
    {"en": "Stone walls, original tiles and a small, cool courtyard.",
     "ar": "جدران حجرية وبلاط أصلي وفناء صغير منعش."},
    {"en": "Made for months: a washer, good light and fast internet.",
     "ar": "مصممة لإقامة أشهر: غسالة وضوء جيد وإنترنت سريع."},
    {"en": "A calm flat full of light, minutes from the park, with a desk and fast internet."},
    {"en": "Three bedrooms, a large living room and a terrace looking over the bay.",
     "ar": "ثلاث غرف نوم وصالون كبير وشرفة تطل على الخليج."},
    {"en": "A small, tidy studio, a few steps from the seafront promenade."},
    {"en": "A new, well-equipped studio in a quiet spot, near cafés and shops.",
     "ar": "استوديو جديد ومجهز جيدًا في مكان هادئ، قرب المقاهي والمحلات."},
    {"ar": "غرفتا نوم وغسالة ومكتب حقيقي، في شارع هادئ قرب السفارات."},
    {"en": "A spacious three-room flat with a large balcony overlooking the city."},
    {"en": "A bright flat a short walk from the sea, ideal for a month in the sun.",
     "ar": "شقة مشرقة على بعد خطوات من البحر، مثالية لشهر تحت الشمس."},
    {"en": "A calm flat right in the centre, minutes from the Sidi M'Cid bridge.",
     "ar": "شقة هادئة في قلب المدينة، على بعد دقائق من جسر سيدي مسيد."},
    {"en": "A comfortable family flat with three rooms, close to shops and schools."},
    {"en": "A quiet studio overlooking the Rhumel gorge, ideal for long stays."},
    {"en": "Everything you need for a month of work: a desk, a washer and fibre internet.",
     "ar": "كل ما تحتاجه لشهر من العمل: مكتب وغسالة وإنترنت بالألياف."},
    {"en": "One bedroom, a bright living room and a balcony in the morning sun.",
     "ar": "غرفة نوم وصالون مشرق وشرفة تدخلها شمس الصباح."},
    {"en": "Large windows onto the Mediterranean, with the seafront at your feet.",
     "ar": "نوافذ واسعة على البحر المتوسط، والواجهة البحرية عند قدميك."},
    {"en": "A traditional house, carefully restored, in the heart of the old town."},
    {"en": "A modern studio with an open view, in a quiet spot near the corniche.",
     "ar": "استوديو حديث بإطلالة مفتوحة، في مكان هادئ قرب الكورنيش."},
    {"ar": "مكتب بجانب النافذة وإنترنت سريع والبحر أمامك طوال اليوم."},
    {"en": "A three-room flat with a large living room, close to the sea and the gardens."},
]

# What a stay's price includes (Details: "What the price includes"). Wi-Fi and utilities always; the rest varies.
INCLUSIONS = ["linen", "weekly_cleaning"]

# Guest reviews, each in its writer's language, handed out to rated stays. Names are first names only.
REVIEWS = [
    ("Hannah", "en", "Spotless and quiet. The desk by the window made a month of work easy."),
    ("Tom", "en", "Exactly as described. Check-in was simple and the host answered within minutes."),
    ("Priya", "en", "Great light all day and a proper kitchen. We'd stay again for longer."),
    ("Léa", "fr", "Très propre, très calme, et la literie est excellente. Je recommande."),
    ("Julien", "fr", "Idéal pour un long séjour : tout est pensé, jusqu'au lave-linge."),
    ("Camille", "fr", "Un quartier vivant et un appartement silencieux. Parfait."),
    ("Inês", "pt", "Casa limpa e luminosa, numa rua sossegada. Voltaria sem hesitar."),
    ("Miguel", "pt", "Tudo funcionou bem e a internet é rápida. Recomendo."),
    ("سارة", "ar", "شقة نظيفة وهادئة، والمضيف متعاون جدًا. أنصح بها."),
    ("كريم", "ar", "موقع ممتاز وكل ما يلزم لإقامة طويلة متوفر."),
    ("ياسمين", "ar", "إضاءة جميلة طوال اليوم ومطبخ مجهز بالكامل."),
    ("Daniel", "en", "Comfortable beds, fast Wi-Fi and a short walk to the station."),
]
REVIEW_MONTHS = ["2026-06", "2026-07", "2026-08", "2026-09", "2026-10"]


AMENITIES = ["wifi", "washer", "workspace", "lift", "kitchen", "air_conditioning", "heating", "parking", "balcony",
             "dishwasher", "tv", "pet_friendly"]
CHECK_IN = ["self_check_in", "host", "lockbox"]


def area_for(rng: random.Random, area: str) -> dict:
    lat, lng = AREAS[area]
    return {"latitude": round(lat + rng.uniform(-0.004, 0.004), 5),
            "longitude": round(lng + rng.uniform(-0.004, 0.004), 5),
            "radiusMetres": rng.choice([250, 300, 350, 400])}


def photos_for(rng: random.Random, pool: dict[str, list[str]], bedrooms: int) -> list[str]:
    """A main room first, then bedrooms, kitchen, bathroom and one or two extras: 5 to 8 photos."""
    picks: list[str] = []

    def take(category: str, count: int = 1) -> None:
        options = [p for p in pool.get(category, []) if p not in picks]
        picks.extend(rng.sample(options, min(count, len(options))))

    take("living")
    take("bedroom", max(1, min(bedrooms, 2)))
    take("kitchen")
    take("bathroom")
    take(rng.choice(["dining", "workspace", "balcony"]))
    take(rng.choice(["dining", "workspace", "balcony", "exterior", "living"]))
    if len(picks) < 8 and rng.random() < 0.5:
        take(rng.choice(["exterior", "balcony", "dining"]))
    return picks[:8]


def blocked_for(rng: random.Random) -> list[dict]:
    """Two or three booked ranges between December 2026 and April 2027."""
    from datetime import date, timedelta
    ranges, day = [], date(2026, 12, 1) + timedelta(days=rng.randint(0, 20))
    for _ in range(rng.randint(2, 3)):
        start = day + timedelta(days=rng.randint(5, 25))
        end = start + timedelta(days=rng.randint(3, 21))
        ranges.append({"start": start.isoformat(), "end": end.isoformat()})
        day = end
    return ranges


def reviews_for(rng: random.Random, rating: dict | None) -> list[dict]:
    """Up to three reviews, newest first, for a stay with a rating; never more than its review count."""
    if rating is None:
        return []
    picks = rng.sample(REVIEWS, min(3, rating["count"]))
    months = sorted(rng.sample(REVIEW_MONTHS, len(picks)), reverse=True)
    return [{"author": author, "month": month, "text": {"text": text, "lang": lang}}
            for (author, lang, text), month in zip(picks, months)]


def main() -> None:
    rng = random.Random(SEED)
    pool_entries = json.loads(POOL.read_text(encoding="utf-8"))["photos"]
    pool: dict[str, list[str]] = {}
    for entry in pool_entries:
        pool.setdefault(entry["category"], []).append(entry["file"])
    cities = {c[0]: c for c in CITIES}

    out_cities = [{
        "id": cid, "countryCode": country, "currency": currency, "latitude": lat, "longitude": lng,
        "photo": pool[f"city_{cid}"][0],
    } for cid, country, currency, (lat, lng), _, _ in CITIES]

    # A second generator for fields added later, so adding them changes none of the values above.
    extras = random.Random(SEED + 1)
    out_stays, counters = [], {}
    for index, (city, area, lang, title, description, bedrooms, beds, baths, sleeps, nightly) in enumerate(STAYS):
        _, _, currency, _, cleaning_range, city_tax = cities[city]
        counters[city] = counters.get(city, 0) + 1
        minor = nightly * 100
        amenities = sorted(set(["wifi", "kitchen", "heating"] + rng.sample(AMENITIES, rng.randint(3, 6))))
        rating = None if rng.random() < 0.12 else {
            "score": round(rng.uniform(4.3, 5.0), 1), "count": rng.choice([2, 7, 12, 18, 27, 32, 46, 61, 88, 124])}
        out_stays.append({
            "id": f"{city}-{counters[city]:02d}",
            "cityId": city,
            "areaKey": area,
            "title": {"text": title, "lang": lang},
            "description": {"text": description, "lang": lang},
            "descriptionTranslations": [{"text": text, "lang": code} for code, text in TRANSLATIONS[index].items()],
            "inclusions": ["wifi", "utilities"] + [i for i in INCLUSIONS if extras.random() < 0.6],
            "facts": {"bedrooms": bedrooms, "beds": beds, "bathrooms": baths, "sleeps": sleeps},
            "photos": photos_for(rng, pool, bedrooms),
            "amenities": amenities,
            "policies": {
                "freeCancellationDaysBefore": rng.choice([7, 14, 30]),
                "deposit": rng.choice([150, 250, 400]) * 100 if currency != "DZD" else rng.choice([20000, 30000]) * 100,
                "depositRefundable": True,
                "checkInFrom": rng.choice(["14:00", "15:00", "16:00"]),
                "checkInMethod": rng.choice(CHECK_IN),
                "checkOutBy": rng.choice(["10:00", "11:00"]),
                "minimumNights": rng.choice([1, 2, 3, 7]),
            },
            "rating": rating,
            "area": area_for(rng, area),
            "pricing": {
                "currency": currency,
                "nightly": minor,
                "cleaningFee": rng.randrange(cleaning_range[0], cleaning_range[1] + 1, 500),
                "cityTaxPerNight": city_tax,
                "longStayDiscountPercent": rng.choice([None, 10, 15, 20]),
                "longStayMinNights": 28,
            },
            "walkMinutesToTransit": rng.choice([None, 3, 5, 8, 12]),
            "instantBook": rng.random() < 0.6,
            "blocked": blocked_for(rng),
            "reviews": reviews_for(extras, rating),
        })

    catalog = {"version": 1, "photoBase": "catalog/photos/", "cities": out_cities, "stays": out_stays}
    text = json.dumps(catalog, ensure_ascii=False, indent=1) + "\n"
    for path in OUTPUTS:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
    print(f"{len(out_cities)} cities, {len(out_stays)} stays -> {', '.join(str(p.relative_to(ROOT)) for p in OUTPUTS)}")


if __name__ == "__main__":
    main()
