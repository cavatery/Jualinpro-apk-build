package com.example.ai

import com.example.model.ProductInput
import com.example.model.PromoType
import com.example.model.TargetAudience

object GeminiPromptBuilder {

    fun buildPromotionPrompt(input: ProductInput): String {
        val targetText = if (input.targetAudience == TargetAudience.CUSTOM) {
            input.customTarget
        } else {
            input.targetAudience.displayName
        }

        val promoPriceText = if (input.promoPrice.isNotBlank()) {
            "Harga Promo: ${input.promoPrice} (Harga Normal: ${input.normalPrice})"
        } else if (input.normalPrice.isNotBlank()) {
            "Harga: ${input.normalPrice}"
        } else {
            "Harga: Hubungi Admin"
        }

        val promoTypeDetail = when (input.promoType) {
            PromoType.NONE -> ""
            PromoType.DISCOUNT -> "Promo: Diskon Khusus ${input.promoDetail}"
            PromoType.BUY_GET -> "Promo: Beli ${input.promoDetail}"
            PromoType.FREE_SHIPPING -> "Promo: Bebas Ongkir ${input.promoDetail}"
            PromoType.FLASH_SALE -> "Promo: Flash Sale Terbatas ${input.promoDetail}"
            PromoType.BONUS -> "Promo: Bonus Hadiah ${input.promoDetail}"
        }

        return """
Kamu adalah AI Copywriter Kelas Dunia spesialis UMKM Indonesia untuk aplikasi "Jualin AI Pro".
Tugasmu adalah menghasilkan materi promosi penjualan LENGKAP, MENARIK, HIPNOTIK, dan SIAP POSTING.

DATA PRODUK:
- Nama Produk: ${input.productName}
- $promoPriceText
- $promoTypeDetail
- Keunggulan / USP: ${input.uspList}
- Target Audiens: $targetText
- Gaya Bahasa Pilihan: ${input.tone.displayName} (${input.tone.description})
- Platform Target: ${input.platform.displayName}
- Nama Toko: ${input.shopName}
- Kontak WhatsApp: ${input.shopContact}
- Lokasi Toko: ${input.shopLocation}

Berikan respons HANYA dalam format JSON valid (tanpa backticks ```json atau markdown apa pun jika memungkinkan, atau jika menggunakan markdown pastikan JSON valid di dalamnya) dengan struktur berikut:

{
  "hookOpening": "1 kalimat pembuka yang sangat memikat / memecah scroll",
  "mainCaption": "Caption promosi lengkap dengan hook, storytelling produk, rincian keunggulan/benefit, harga promo, ajakan bertindak (CTA), dan kontak WhatsApp/toko",
  "description": "Deskripsi produk yang elegan dan menggugah selera/kebutuhan",
  "advantagesAndBenefits": "Poin-poin keunggulan dan benefit nyata bagi pembeli",
  "callToAction": "Kalimat Call to Action yang mendesak dan jelas (misal: 'Klik link di bio sekarang!')",
  "hashtags": ["#Tagar1", "#Tagar2", "#Tagar3", "#Tagar4", "#Tagar5", "#Tagar6", "#Tagar7", "#Tagar8"],
  "alternativeTitles": [
    "3 Judul / Headline Alternatif Menarik 1",
    "3 Judul / Headline Alternatif Menarik 2",
    "3 Judul / Headline Alternatif Menarik 3"
  ],
  "viralHooks": [
    "Hook 1: Pertanyaan menggelitik",
    "Hook 2: Fakta mengejutkan / FOMO",
    "Hook 3: Solusi instan untuk masalah konsumen"
  ],
  "ctaVariations": [
    "CTA 1: Desakan stok terbatas",
    "CTA 2: Promo hari ini saja",
    "CTA 3: Konsultasi gratis via WhatsApp"
  ],
  "adVariations": {
    "whatsapp": "Teks broadcast WhatsApp lengkap dengan emoji rapi dan nomor kontak",
    "wa_status": "Teks singkat padat untuk status WA (Story)",
    "instagram": "Caption Instagram estetik dengan format rapi dan hashtag",
    "facebook": "Copywriting Facebook / Facebook Ads persuasif ramah komunitas",
    "tiktok_script": "Naskah video TikTok 15-30 detik: [Visual: ...] [Voiceover/Teks: ...]",
    "marketplace": "Deskripsi produk SEO-friendly untuk Shopee/Tokopedia"
  },
  "weeklyPlan": [
    {
      "dayNumber": 1,
      "dayName": "Senin",
      "theme": "Problem & Solusi",
      "concept": "Tunjukkan masalah yang sering dialami konsumen dan bagaimana produk ini menyelesaikannya",
      "readyCaption": "Caption siap posting untuk Senin..."
    },
    {
      "dayNumber": 2,
      "dayName": "Selasa",
      "theme": "Keunggulan Bahan / Kualitas",
      "concept": "Behind the scene atau kualitas bahan premium produk",
      "readyCaption": "Caption siap posting untuk Selasa..."
    },
    {
      "dayNumber": 3,
      "dayName": "Rabu",
      "theme": "Testimoni & Social Proof",
      "concept": "Cerita kepuasan pelanggan yang sudah merasakan manfaatnya",
      "readyCaption": "Caption siap posting untuk Rabu..."
    },
    {
      "dayNumber": 4,
      "dayName": "Kamis",
      "theme": "Tips & Edukasi",
      "concept": "Tips bermanfaat yang berhubungan dengan kategori produk",
      "readyCaption": "Caption siap posting untuk Kamis..."
    },
    {
      "dayNumber": 5,
      "dayName": "Jumat",
      "theme": "Jumat Berkah & Penawaran Spesial",
      "concept": "Promo akhir pekan atau penawaran terbatas",
      "readyCaption": "Caption siap posting untuk Jumat..."
    },
    {
      "dayNumber": 6,
      "dayName": "Sabtu",
      "theme": "Gaya Hidup & Interaksi",
      "concept": "Ajak audiens berinteraksi di kolom komentar",
      "readyCaption": "Caption siap posting untuk Sabtu..."
    },
    {
      "dayNumber": 7,
      "dayName": "Minggu",
      "theme": "Last Call / Reminder Weekend",
      "concept": "Pengingat terakhir sebelum promo mingguan berakhir",
      "readyCaption": "Caption siap posting untuk Minggu..."
    }
  ],
  "storytelling": "Paragraf storytelling emosional tentang perjuangan / alasan dibuatnya produk ini untuk menyentuh hati pembeli",
  "promoCopy": "Naskah diskon / promo terbatas yang menciptakan rasa urgensi (FOMO)",
  "quickReplies": [
    {
      "id": "qr_1",
      "questionCategory": "Tanya Harga",
      "questionSample": "Halo kak, harganya berapa ya?",
      "suggestedReply": "Halo kak! Untuk ${input.productName} harganya $promoPriceText kak. Sedang ada promo khusus hari ini ya kak, mau dikirim ke kota mana kak? 😊"
    },
    {
      "id": "qr_2",
      "questionCategory": "Tanya Ongkir",
      "questionSample": "Bisa kirim ke tempat saya? Ongkir berapa?",
      "suggestedReply": "Bisa banget kak! Kami melayani pengiriman ke seluruh Indonesia dari ${input.shopLocation}. Boleh minta detail kecamatan dan kotanya kak agar saya bantu cekkan promo gratis ongkirnya? 📦"
    },
    {
      "id": "qr_3",
      "questionCategory": "Tanya Stok / Varian",
      "questionSample": "Barangnya ready stock kak?",
      "suggestedReply": "Ready stock kak, siap kirim hari ini! Tapi stok untuk batch promo ini sangat terbatas kak. Mau kami amankan sekarang? ✨"
    }
  ],
  "followUps": [
    {
      "id": "fu_1",
      "title": "Follow-up 3 Jam (Belum Transfer)",
      "targetBuyerCondition": "Konsumen sudah tanya tapi belum lanjut checkout / transfer",
      "messageText": "Halo kak 😊 Mau info, pesanan ${input.productName} kakak sudah kami siapkan ya. Mau sekalian dikirim sore ini kak? Biar besok langsung sampai ke alamat kakak!"
    },
    {
      "id": "fu_2",
      "title": "Follow-up Besok Pagi (Ingatkan Promo)",
      "targetBuyerCondition": "Konsumen menghilang setelah tanya harga",
      "messageText": "Selamat pagi kak! Semoga harinya menyenangkan ✨ Mau mengingatkan kuota promo spesial ${input.productName} tersisa 2 slot lagi untuk hari ini kak. Mau diamankan sekarang?"
    }
  ]
}
""".trimIndent()
    }

    fun buildPhotoAnalysisPrompt(productName: String): String {
        return """
Analisis foto produk ini untuk kebutuhan pemasaran UMKM Indonesia.
Nama Produk (jika ada): $productName

Berikan output JSON dengan format:
{
  "productType": "Kategori / Jenis Produk",
  "detectedColors": "Warna dominan & aksen visual",
  "packagingType": "Tipe kemasan / presentasi produk",
  "productVibe": "Kesan/Vibe visual (misal: Segar, Renyah, Elegan, Alami, Modern)",
  "suggestedAudience": "Target audiens yang paling cocok dengan tampilan visual ini",
  "suggestedHooks": [
    "Rekomendasi hook 1 berdasarkan foto",
    "Rekomendasi hook 2 berdasarkan foto",
    "Rekomendasi hook 3 berdasarkan foto"
  ]
}
""".trimIndent()
    }
}
