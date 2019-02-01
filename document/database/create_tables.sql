-- DROP ALL TABLE

DROP TABLE IF EXISTS public."TB_YNA_HEADLINE_NEWS";

CREATE TABLE IF NOT EXISTS public."TB_YNA_HEADLINE_NEWS" (
	link varchar(128) NOT NULL,
	datetime timestamp NOT NULL,
	title varchar(256) NOT NULL,
	summary varchar(256) NOT NULL,
	publishing_datetime timestamp NOT NULL,
	"offset" serial NOT NULL,
	CONSTRAINT "TB_YNA_HEADLINE_NEWS_PK" PRIMARY KEY (link)
);

CREATE INDEX IF NOT EXISTS "TB_YNA_HEADLINE_NEWS_IDX_autoIncId" ON public."TB_YNA_HEADLINE_NEWS" USING btree ("offset");


-- DROP TABLE IF EXISTS

DROP TABLE IF EXISTS public."TB_YNA_HOT_NEWS";

CREATE TABLE IF NOT EXISTS public."TB_YNA_HOT_NEWS" (
	link varchar(128) NOT NULL,
	datetime timestamp NOT NULL,
	title varchar(256) NOT NULL,
	summary varchar(256) NOT NULL,
	publishing_datetime timestamp NOT NULL,
	"offset" serial NOT NULL,
	CONSTRAINT "TB_YNA_HOT_NEWS_PK" PRIMARY KEY (link)
);

CREATE INDEX IF NOT EXISTS "TB_YNA_HOT_NEWS_IDX_autoIncId" ON public."TB_YNA_HOT_NEWS" USING btree ("offset");

-- DROP TABLE IF EXISTS

DROP TABLE IF EXISTS public."TB_YNA_MOST_VIEW_RANK_NEWS";

CREATE TABLE IF NOT EXISTS public."TB_YNA_MOST_VIEW_RANK_NEWS" (
	datetime timestamp NOT NULL,
	"rank" int4 NOT NULL,
	link varchar(128) NOT NULL,
	title varchar(256) NOT NULL,
	summary varchar(256) NOT NULL,
	publishing_datetime timestamp NOT NULL,
	"offset" serial NOT null,
	CONSTRAINT "TB_YNA_MOST_VIEW_RANK_NEWS_PK" PRIMARY KEY (datetime, "rank")
);

CREATE INDEX IF NOT EXISTS "TB_YNA_MOST_VIEW_RANK_NEWS_PK_REORDER" ON public."TB_YNA_MOST_VIEW_RANK_NEWS" USING btree (datetime desc, "rank" asc);
CREATE INDEX IF NOT EXISTS "TB_YNA_MOST_VIEW_RANK_NEWS_autoIncId" ON public."TB_YNA_MOST_VIEW_RANK_NEWS" USING btree ("offset");

-- DROP TABLE IF EXISTS

DROP TABLE IF EXISTS public."TB_YNA_LAST_NEWS_LINK";

CREATE TABLE IF NOT EXISTS public."TB_YNA_LAST_NEWS_LINK" (
	news_url varchar(64) NOT NULL,
	link varchar(128) NOT NULL,
	update_datetime timestamp NOT NULL,
	CONSTRAINT "TB_YNA_LAST_NEWS_LINK_PK" PRIMARY KEY (news_url)
);

-- CREATE INDEX IF NOT EXISTS "TB_YNA_LAST_NEWS_LINK_IDX_link" ON public."TB_YNA_LAST_NEWS_LINK_PK" USING btree (link);