package com.example.scrapper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ProgressDialog p;
    static String inputText;
    String lastUse;
    int from;
    int page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout);
        EditText input = findViewById(R.id.input);
        ImageView searchDeca = findViewById(R.id.img_search_deca);
        ImageView searchMarmiton = findViewById(R.id.img_search_marmiton);
        listView = (ListView) findViewById(R.id.list);

        searchDeca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(String.valueOf(input.getText()));
                if (!String.valueOf(input.getText()).equals("")) {
                    from=0;
                    MainActivity.inputText= String.valueOf(input.getText());
                    Webscrapping webscrappingDecathlon=new Webscrapping();
                    webscrappingDecathlon.execute("https://www.decathlon.fr/search?Ntt="+input.getText()+"&size=8");
                    from=8;
                    lastUse="decathlon";
                }
                else {
                    Toast.makeText(MainActivity.this,"Veuillez d'abord un article à rechercher",Toast.LENGTH_LONG).show();
                }

            }
        });
        searchMarmiton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(String.valueOf(input.getText()));
                if (!String.valueOf(input.getText()).equals("")) {
                    MainActivity.inputText= String.valueOf(input.getText());
                    Webscrapping webscrappingMarmiton=new Webscrapping();
                    webscrappingMarmiton.execute("https://www.marmiton.org/recettes/recherche.aspx?aqt="+input.getText());
                    page=1;
                    lastUse="marmiton";
                }
                else {
                    Toast.makeText(MainActivity.this,"Veuillez d'abord une recette à rechercher",Toast.LENGTH_LONG).show();
                }

            }
        });
        FloatingActionButton nextPage = findViewById(R.id.floatingActionButton);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(String.valueOf(input.getText()));
                if (!MainActivity.inputText.equals("")) {
                    if (lastUse.equals("decathlon")){
                        Webscrapping webscrappingNextPage=new Webscrapping();
                        webscrappingNextPage.execute("https://www.decathlon.fr/search?Ntt="+MainActivity.inputText+"&size=8&from="+from);
                        from+=8;
                    }
                    else if (lastUse.equals("marmiton")){
                        page+=1;
                        Webscrapping webscrappingNextPage=new Webscrapping();
                        webscrappingNextPage.execute("https://www.marmiton.org/recettes/recherche.aspx?aqt="+MainActivity.inputText+"&page="+page);

                    }
                }
                else {
                    Toast.makeText(MainActivity.this,"Veuillez d'abord un article/ à rechercher",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private class Webscrapping extends AsyncTask<String, String, ArrayList<Article>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(MainActivity.this);
            p.setMessage("Please wait...It is downloading");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected ArrayList<Article> doInBackground(String... strings) {
            Document document = null;
            try {
               document  = Jsoup.connect(strings[0]).get();
            }catch (IOException e){
                Log.e("Error",e.toString());
            }
            if (strings[0].contains("https://www.decathlon.fr/")){

                //decathlon
                Elements listeTitreComplet = document.getElementsByClass("vtmn-p-0 vtmn-m-0 vtmn-text-sm vtmn-font-normal vtmn-overflow-hidden vtmn-text-ellipsis svelte-1l3biyf");
                Elements listePhotoComplet = document.getElementsByClass("svelte-w1lrdd");
                Elements listePrixComplet = document.getElementsByClass("vtmn-price_size--medium");
                Elements listeLienComplet = document.getElementsByClass("dpb-product-model-link");

                List<String> listePhoto = new ArrayList<>();
                for (Element photo: listePhotoComplet ) {
                    //class du parent : dpb-models
                    boolean addPhoto= false;
                    Elements ancestors= photo.parents();
                    for (Element ancestor: ancestors) {
                        if (ancestor.hasClass("dpb-models")){
                            addPhoto=true;
                        }
                    }
                    if (addPhoto){
                        listePhoto.add(photo.attr("src"));
                    }
                }
                List<String> listeTitre = new ArrayList<>();
                for (Element titre: listeTitreComplet) {
                    listeTitre.add(titre.text());
                }
                List<String> listePrix = new ArrayList<>();
                for (Element prix: listePrixComplet) {
                    listePrix.add(prix.text());
                }
                List<Uri> listeLien = new ArrayList<>();
                for (Element lien: listeLienComplet) {
                    listeLien.add(Uri.parse("https://decathlon.fr"+lien.attr("href")));
                }
                ArrayList<Article> listeArticles = new ArrayList<Article>();
                for(int i = 0 ; i < listePrix.size(); i++) {
                    listeArticles.add(new Article(listeTitre.get(i),listePrix.get(i),listePhoto.get(i),listeLien.get(i)));
                }
                return listeArticles;
            } else if (strings[0].contains("https://www.marmiton.org/")){
                Elements listeTitreComplet = document.getElementsByClass("MRTN__sc-30rwkm-0 dJvfhM");
                Elements listePhotoComplet = document.getElementsByClass("SHRD__sc-dy77ha-0");
                Elements listeNoteComplet = document.getElementsByClass("SHRD__sc-10plygc-0 jHwZwD");
                Elements listeLienComplet = document.getElementsByClass("MRTN__sc-1gofnyi-2 gACiYG");

                List<String> listePhoto = new ArrayList<>();
                for (Element photo: listePhotoComplet) {
                    if (photo.equals(listePhotoComplet.first())){
                    listePhoto.add(photo.attr("src"));}
                    else {listePhoto.add(photo.attr("data-src"));}
                }
                List<String> listeTitre = new ArrayList<>();
                for (Element titre: listeTitreComplet) {
                    listeTitre.add(titre.text());
                }
                List<String> listeNote = new ArrayList<>();
                for (Element prix: listeNoteComplet) {
                    listeNote.add(prix.text());
                }
                List<Uri> listeLien = new ArrayList<>();
                for (Element lien: listeLienComplet) {
                    listeLien.add(Uri.parse("https://marmiton.org"+lien.attr("href")));
                }
                ArrayList<Article> listeArticles = new ArrayList<Article>();
                for(int i = 0 ; i < listeNote.size(); i++) {
                    listeArticles.add(new Article(listeTitre.get(i),listeNote.get(i),listePhoto.get(i),listeLien.get(i)));
                }
                return listeArticles;
            }
            else {
                System.out.println("there is an error");
                return null;
            }
        }


        protected void onPostExecute(ArrayList<Article>  liste) {

            CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, liste);
            listView.setAdapter(customAdapter);
            p.hide();

        }
    }
}