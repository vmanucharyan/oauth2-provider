# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table "OAUTH_APPS" ("id" CHAR(40) NOT NULL PRIMARY KEY,"secret" CHAR(255) NOT NULL,"user_id" VARCHAR NOT NULL);
create table "USERS" ("email" VARCHAR NOT NULL PRIMARY KEY,"full_name" VARCHAR NOT NULL,"pass_hash" CHAR(256) NOT NULL);
alter table "OAUTH_APPS" add constraint "fk_user_id" foreign key("user_id") references "USERS"("email") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "OAUTH_APPS" drop constraint "fk_user_id";
drop table "USERS";
drop table "OAUTH_APPS";

