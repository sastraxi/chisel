package com.lyricfind.dpgscript.processes;

import com.lyricfind.dpgscript.JDBCProcess;
import com.lyricfind.dpgscript.annotations.Required;
import com.lyricfind.dpgscript.annotations.Unique;

public class ProductionDelta extends JDBCProcess {

    public void setup(@Unique String jdbcURI) throws Exception
    {
        super.setup(jdbcURI, ";\n");
        addStatement("USE `catalog`");
        addStatement("SELECT 1");
        addResource("sql/production_delta.sql");
    }
}