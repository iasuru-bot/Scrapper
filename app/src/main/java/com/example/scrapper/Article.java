package com.example.scrapper;

import android.net.Uri;

public class Article {
        String titre;
        String prix;
        String image;
        Uri lien;
        public Article(String titre, String prix, String image,Uri lien) {
            this.titre = titre;
            this.prix = prix;
            this.image = image;
            this.lien = lien;
        }
    }

