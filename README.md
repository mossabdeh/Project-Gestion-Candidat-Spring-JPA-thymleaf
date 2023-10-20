# Gestion Doctorat Web Application
**Live Code :**

https://gestion-doctorat-production.up.railway.app


**Home Page:**
![hahaPNG](https://github.com/mossabdeh/Project-Gestion-Candidat-Spring-JPA-thymleaf/assets/79877072/7723f838-8fd6-4852-a219-bf29e576b71c)


**Login:**

![image1](https://github.com/mossabdeh/Project-Gestion-Candidat-Spring-JPA-thymleaf/assets/79877072/72318e61-6817-4bc6-9856-954c8c234f94)


| Role                  | Username              | Password |
| --------------------- | --------------------- | ---------|
| **Admin**             | admin                 | admin    |
| **VD**                | Vd                    | vd       |
| **CFD**               | Cfd                   | Cfd      |
| **Candidat1**         | mossab0               | mossab   |
| **Candidat2**         | hakim0                | hakim    |
| **Enseignant1(Ens)**  | prof                  | prof     |
| **Enseignant2**       | doc                   | doc      |


## **Introduction :**
>
> L\'organisation d\'un concours de doctorat est un processus complexe
> qui nécessite une gestion rigoureuse et précise pour assurer l\'équité
> et la transparence du processus de sélection.
>
> L\'université de Constantine 2 organise chaque année un concours de
> doctorat pour recruter les meilleurs candidats pour ses programmes de
> troisième cycle LMD.
>
> Afin d\'optimiser ce processus et de simplifier la tâche des
> différents acteurs impliqués dans la gestion du concours, nous avons
> développé une application web de gestion de concours de doctorat qui
> permet de suivre le processus de bout en bout, depuis la soumission
> des candidatures jusqu\'à l\'affichage des résultats finaux.
>
> L\'objectif de cette application est de simplifier la gestion du
> concours en offrant une plateforme en ligne qui permet aux candidats
> de soumettre leur candidature, de vérifier leur admissibilité et de
> consulter les informations importantes fournies par le vice-doyen,
> tout en permettant au président du CFD d\'attribuer des enseignants
> pour la correction des copies, de saisir les notes, de calculer les
> moyennes finales et d\'afficher la liste des candidats avec leurs
> notes.
>
> Dans cette documentation, nous allons détailler les différentes
> fonctionnalités de l\'application et expliquer comment elle répond aux
> exigences fonctionnelles et non fonctionnelles identifiées lors de la
> phase de conception.

#Etablissement du cahier des charges

### **La problématique :**
>
> La problématique de l\'application de gestion de concours de doctorat
> de l\'université de Constantine 2 est de simplifier et d\'optimiser le
> processus de gestion du concours en offrant une plateforme en ligne
> qui permet aux candidats de soumettre leur candidature, de vérifier
> leur admissibilité et de consulter les informations importantes
> fournies par le vice-doyen, tout en permettant au président du CFD
> d\'attribuer des enseignants pour la correction des copies, de saisir
> les notes, de calculer les moyennes finales et d\'afficher la liste
> des candidats avec leurs notes.
>
> Le processus de gestion manuel de ce concours peut être fastidieux et
> coûteux en termes de temps et de ressources, et peut également
> entraîner des erreurs humaines qui peuvent nuire à l\'équité et à la
> transparence du processus de sélection. Par conséquent, cette
> application offre une solution pour rationaliser ce processus en
> utilisant des outils et des fonctionnalités en ligne qui garantissent
> une gestion efficace et précise du concours.

## Les Acteurs :

- **Admin**: En tant qu'administrateur, votre rôle est d'effectuer des opérations CRUD (Create, Read, Update, Delete) pour les utilisateurs du système. Vous êtes responsable de la gestion des comptes des utilisateurs, y compris la création de nouveaux comptes, la lecture des informations utilisateur existantes, la mise à jour des détails utilisateur et la suppression des comptes si nécessaire. Vous êtes également chargé de maintenir la confidentialité et la sécurité des données des utilisateurs. Votre interaction avec le système se fait en français.

- **Vice-doyen**: Dans le contexte de ce texte, le vice-doyen est un responsable de l'université de Constantine 2 chargé de partager des informations nécessaires sur le déroulement du concours du 3ème cycle LMD doctorat et de générer des codes pour la préservation de l'anonymat des candidats présents lors du concours.

- **CFD**: Le CFD est l'acronyme de "Conseil de la Formation et de la Documentation". Dans ce contexte, le président du CFD est responsable de l'affectation d'un ensemble d'enseignants pour la correction des copies des examens des candidats.

- **Enseignants**: Les enseignants sont des personnes chargées de corriger les copies des examens des candidats. Chaque copie est corrigée par deux enseignants, et si la différence entre les deux notes est supérieure ou égale à 3 points, le président du CFD doit affecter un troisième enseignant pour corriger cette copie.

- **Candidats**: Les candidats sont les étudiants qui ont postulé pour le concours du 3ème cycle LMD doctorat de l'université de Constantine 2. Ils sont autorisés à consulter toutes les informations partagées par le vice-doyen via l'application web développée, et ils peuvent également consulter leurs propres notes une fois qu'elles sont publiées.


# Les exigences fonctionnelles et non fonctionnelles :

> une liste des exigences fonctionnelles et non fonctionnelles pour
> l\'application web de gestion de concours de doctorat de l\'université
> de Constantine 2 :

## Exigences fonctionnelles :

-   Le vice-doyen peut publier des informations importantes concernant
    le déroulement du concours.

-   Le vice-doyen doit générer le code pour préserver l\'anonymat des
    candidats présents lors du concours

-   Le président du CFD doit attribuer les enseignants pour la
    correction des copies d\'examen des candidats.

-   Le président du CFD doit afficher les résultats et les moyennes des
    candidats.

-   Admin est responsable de la gestion des comptes :

    -   Admin doit créer des comptes pour candidats, les enseignants.

    -   Admin peut Afficher les comptes pour candidats, les enseignants.

    -   Admin peut modifier les comptes de tous les utilisateurs (Admin,
        Candidat, Enseignant, CFD, Vice Doyen)

    -   Admin peut supprimer les comptes de tous les utilisateurs
        (Admin, Candidat, Enseignant, CFD, Vice Doyen)

-   le Candidat peut consulter les informations partagées par le Vice
    Doyen.

-   le Candidat peut consulter ses résultats et Classement de Doctorat
    (admis ou pas).

-   L'enseignant affecter par le CFD doit saisir les notes des
    candidats.

-   L'enseignant affecter par le CFD doit envoyer les notes saisies des
    candidats.

## Exigences non fonctionnelles :

- •Interface utilisateur conviviale et facile à utiliser pour tous les utilisateurs (candidats, enseignants, président du CFD, etc.)

- •Sécurité robuste pour protéger les données des utilisateurs et
   garantir la confidentialité de l\'anonymat des candidats(hachages des mot de passes , Authorization ...)

- •Performance élevée pour assurer une réponse rapide aux demandes des utilisateurs, même lors de pics de trafic

- •Disponibilité continue de l\'application web pour garantir que les utilisateurs peuvent accéder aux informations à tout moment.

- •Compatibilité avec différents navigateurs web et appareils (ordinateurs, smartphones, tablettes, etc.) 
    pour garantir que tous les utilisateurs peuvent accéder à l\'application

## Les Diagrammes :

1.  **Diagramme de case d'utilisation :**

![image2](https://github.com/mossabdeh/Project-Gestion-Candidat-Spring-JPA-thymleaf/assets/79877072/50be1ffb-8e24-4b67-8ce9-be677bb197a4)


# Diagramme de Classe () :

![image3](https://github.com/mossabdeh/Project-Gestion-Candidat-Spring-JPA-thymleaf/assets/79877072/6cf86993-f8d4-4005-b48d-879e53474ade)



# Diagramme de Base de Donnés :
![image4](https://github.com/mossabdeh/Project-Gestion-Candidat-Spring-JPA-thymleaf/assets/79877072/6c748ffc-90c7-46dd-ac8e-b504a69723c3)



