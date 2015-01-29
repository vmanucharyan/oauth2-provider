# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "ALBUMS" ("name" VARCHAR NOT NULL,"description" VARCHAR NOT NULL,"year" INTEGER NOT NULL,"artist_id" BIGINT NOT NULL,"id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY);
create table "ARTISTS" ("name" VARCHAR NOT NULL,"description" VARCHAR NOT NULL,"id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY);
create table "OAUTH_APPS" ("id" VARCHAR NOT NULL PRIMARY KEY,"secret" VARCHAR NOT NULL,"user_id" VARCHAR NOT NULL);
create table "OAUTH_REDIRECT_URL" ("id" BIGINT NOT NULL PRIMARY KEY,"app_id" VARCHAR NOT NULL);
create table "SONGS" ("name" VARCHAR NOT NULL,"genre" VARCHAR NOT NULL,"duration_sec" INTEGER NOT NULL,"album_id" BIGINT NOT NULL,"artist_id" BIGINT NOT NULL,"id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY);
create table "USERS" ("email" VARCHAR NOT NULL PRIMARY KEY,"full_name" VARCHAR NOT NULL,"pass_hash" CHAR(256) NOT NULL);
alter table "ALBUMS" add constraint "artist_fk" foreign key("artist_id") references "ALBUMS"("id") on update NO ACTION on delete NO ACTION;
alter table "OAUTH_APPS" add constraint "fk_user_id" foreign key("user_id") references "USERS"("email") on update NO ACTION on delete NO ACTION;
alter table "OAUTH_REDIRECT_URL" add constraint "fk_app_id" foreign key("app_id") references "OAUTH_APPS"("id") on update NO ACTION on delete NO ACTION;
alter table "SONGS" add constraint "album_fk" foreign key("album_id") references "SONGS"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "SONGS" drop constraint "album_fk";
alter table "OAUTH_REDIRECT_URL" drop constraint "fk_app_id";
alter table "OAUTH_APPS" drop constraint "fk_user_id";
alter table "ALBUMS" drop constraint "artist_fk";
drop table "USERS";
drop table "SONGS";
drop table "OAUTH_REDIRECT_URL";
drop table "OAUTH_APPS";
drop table "ARTISTS";
drop table "ALBUMS";

