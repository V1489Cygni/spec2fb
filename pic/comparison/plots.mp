verbatimtex
%&latex
\documentclass{article}
\usepackage{amsmath}
\usepackage[utf8]{inputenc} % в кодировке UTF-8

\usepackage[T2A,OT1]{fontenc} % шрифты
\usepackage[russian]{babel} % русификация

\begin{document}
etex
                     
input graph;

beginfig(1)
    vardef TEX primary s =
	write "verbatimtex"                    to "mptextmp.mp";
	write "\documentclass[12pt]{article}"  to "mptextmp.mp";
	write "\usepackage[T1]{fontenc}"       to "mptextmp.mp";
	write "\usepackage{amsmath,amssymb}"   to "mptextmp.mp";
	write "\begin{document}"               to "mptextmp.mp";
	write "etex"                           to "mptextmp.mp";
	write "btex "&s&" etex"                to "mptextmp.mp";
	write EOF                              to "mptextmp.mp";
	scantokens "input mptextmp"
    enddef;

    color c[];
    c[0] := black;
    c[1] := black;
    picture p[], legenda;
    legenda := nullpicture;

    picture marker[];
    numeric Symbolsize; Symbolsize := 1mm;
    fill fullcircle scaled Symbolsize withcolor white;
    draw fullcircle scaled Symbolsize;
    marker[0] := currentpicture;
    currentpicture := nullpicture;
    draw ((-0.5,-0.5) -- (-0.5,0.5)) scaled Symbolsize ;
    draw ((-0.5,0.5) -- (0.5,0.5)) scaled Symbolsize ;
    draw ((0.5,0.5) -- (0.5,-0.5)) scaled Symbolsize ;
    draw ((0.5,-0.5) -- (-0.5,-0.5)) scaled Symbolsize ;
    marker[1] := currentpicture ;
    currentpicture := nullpicture;
    
    p[1] := nullpicture;
    addto p[1] also btex $pMuACO$ etex 
	withcolor c[1];
    p[0] := nullpicture;
    addto p[0] also btex $psMuACO$ etex 
	withcolor c[0];

    addto legenda doublepath ( (0,0)--(5mm,0) ) 
	shifted (0, -5mm*(0 - 1))
	withpen currentpen withcolor c[0] dashed evenly;
    addto legenda also marker[0]
	shifted (0, -5mm*(0 - 1));
    addto legenda also marker[0]
	shifted (5mm, -5mm*(0 - 1));
    addto legenda also p[0]
	shifted 1/2(lrcorner p[0] - ulcorner p[0])
	shifted (0, -5mm*(0 - 1));


    addto legenda doublepath ( (0,0)--(5mm,0) ) 
	shifted (0, -5mm*(1 - 1))
	withpen currentpen withcolor c[1];
    addto legenda also marker[1]
	shifted (0, -5mm*(1 - 1));
    addto legenda also marker[1]
	shifted (5mm, -5mm*(1 - 1));
    addto legenda also p[1]
	shifted 1/2(lrcorner p[0] - ulcorner p[0])
	shifted (0, -5mm*(1 - 1));

 
    draw begingraph(3in, 1.5in);
        glabel.bot(btex \small Число состояний автомата etex, OUT);
        glabel.lft(btex \small Медианное время, c. etex rotated 90, OUT);
        gdraw "parallel-muaco.dat" withcolor c[0] withpen pencircle scaled 1pt;

	path parallelMuaco;
        gdata("parallel-muaco.dat", s, augment.parallelMuaco(s1, s2); );
	N:=length parallelMuaco; 
	for n=0 upto N-1: 
	    x:=xpart point n of parallelMuaco; 
	    y:=ypart point n of parallelMuaco;             
	    glabel(marker[1], x, y);
	endfor; 
        x:=xpart point N of parallelMuaco; 
        glabel(marker[1], x, "11621.48");

        gdraw "parallel-sat-muaco.dat" withcolor c[1] withpen pencircle scaled 1pt dashed evenly;
	path parallelSat;
	gdata("parallel-sat-muaco.dat", s, augment.parallelSat(s1, s2); );
	N:=length parallelSat; 
	for n=0 upto N: 
	    x:=xpart point n of parallelSat; 
	    y:=ypart point n of parallelSat; 
	    glabel(marker[0], x, y);
	endfor; 

%        autogrid(grid.bot, grid.lft) withcolor .85white;
        autogrid(, otick.lft);
        otick.bot(btex 10 etex, 10);
        otick.bot(btex 15 etex, 15);
        otick.bot(btex 20 etex, 20);

        glabel.top(legenda, OUT);
    endgraph;
endfig;


beginfig(2)
    vardef TEX primary s =
	write "verbatimtex"                    to "mptextmp.mp";
	write "\documentclass[12pt]{article}"  to "mptextmp.mp";
	write "\usepackage[T1]{fontenc}"       to "mptextmp.mp";
	write "\usepackage{amsmath,amssymb}"   to "mptextmp.mp";
	write "\begin{document}"               to "mptextmp.mp";
	write "etex"                           to "mptextmp.mp";
	write "btex "&s&" etex"                to "mptextmp.mp";
	write EOF                              to "mptextmp.mp";
	scantokens "input mptextmp"
    enddef;

    picture marker[];
    numeric Symbolsize; Symbolsize := 1mm;
    fill fullcircle scaled Symbolsize withcolor white;
    draw fullcircle scaled Symbolsize;
    marker[0] := currentpicture;
    currentpicture := nullpicture;
    draw ((-0.5,-0.5) -- (-0.5,0.5)) scaled Symbolsize ;
    draw ((-0.5,0.5) -- (0.5,0.5)) scaled Symbolsize ;
    draw ((0.5,0.5) -- (0.5,-0.5)) scaled Symbolsize ;
    draw ((0.5,-0.5) -- (-0.5,-0.5)) scaled Symbolsize ;
    marker[1] := currentpicture ;
    currentpicture := nullpicture;


    color c[];
    c[0] := black;
    c[1] := black;
    picture p[], legenda;
    legenda := nullpicture;
    
    p[0] := nullpicture;
    addto p[0] also btex $psMuACO$ etex 
	withcolor c[0];
    p[1] := nullpicture;
    addto p[1] also btex $pstMuACO$ etex 
	withcolor c[1];

    addto legenda doublepath ( (0,0)--(5mm,0) ) 
	shifted (0, -5mm*(0 - 1))
	withpen currentpen withcolor c[0] dashed evenly;
    addto legenda also marker[0]
	shifted (0, -5mm*(0 - 1));
    addto legenda also marker[0]
	shifted (5mm, -5mm*(0 - 1));
    addto legenda also p[0]
	shifted 1/2(lrcorner p[0] - ulcorner p[0])
	shifted (0, -5mm * (0 - 1));


    addto legenda doublepath ( (0,0)--(5mm,0) ) 
	shifted (0, -5mm*(1 - 1))
	withpen currentpen withcolor c[1];
    addto legenda also marker[1]
	shifted (0, -5mm*(1 - 1));
    addto legenda also marker[1]
	shifted (5mm, -5mm*(1 - 1));
    addto legenda also p[1]
	shifted 1/2(lrcorner p[0] - ulcorner p[0])
	shifted (0, -5mm * (1 - 1));

 
    draw begingraph(3in, 1.5in);
        glabel.bot(btex \small Число состояний автомата etex, OUT);
        glabel.lft(btex \small Медианное время, c. etex rotated 90, OUT);
        gdraw "parallel-sat-muaco.dat" withcolor c[0] withpen pencircle scaled 1pt dashed evenly;

	path parallelSat;
	gdata("parallel-sat-muaco.dat", s, augment.parallelSat(s1, s2); );
	N:=length parallelSat; 
	for n=0 upto N: 
	    x:=xpart point n of parallelSat; 
	    y:=ypart point n of parallelSat; 
	    glabel(marker[0], x, y);
	endfor; 

        gdraw "throw-away.dat" withcolor c[1] withpen pencircle scaled 1pt;
	path throwAwaySat;
	gdata("throw-away.dat", s, augment.throwAwaySat(s1, s2); );
	N:=length throwAwaySat; 
	for n=0 upto N: 
	    x:=xpart point n of throwAwaySat; 
	    y:=ypart point n of throwAwaySat; 
	    glabel(marker[1], x, y);
	endfor; 


        %autogrid(grid.bot, grid.lft) withcolor .85white;
        autogrid(, otick.lft);
        otick.bot(btex 10 etex, 10);
        otick.bot(btex 15 etex, 15);
        otick.bot(btex 20 etex, 20);

        glabel.top(legenda, OUT);
    endgraph;
endfig;


end.
