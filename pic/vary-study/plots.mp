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
 
    picture marker[];
    numeric Symbolsize; Symbolsize := 1mm;
    fill fullcircle scaled Symbolsize withcolor white;
    draw fullcircle scaled Symbolsize;
    marker[0] := currentpicture;
    currentpicture := nullpicture; 
    fill fullcircle scaled Symbolsize ;
    marker[1] := currentpicture ;
    currentpicture := nullpicture;
    draw ((-0.5,-0.5) -- (0.5,0.5)) scaled Symbolsize ;
    draw ((0.5,-0.5) -- (-0.5,0.5)) scaled Symbolsize ;
    marker[2] := currentpicture ;
    currentpicture := nullpicture;
    draw ((-0.5,-0.5) -- (-0.5,0.5)) scaled Symbolsize ;
    draw ((-0.5,0.5) -- (0.5,0.5)) scaled Symbolsize ;
    draw ((0.5,0.5) -- (0.5,-0.5)) scaled Symbolsize ;
    draw ((0.5,-0.5) -- (-0.5,-0.5)) scaled Symbolsize ;
    marker[3] := currentpicture ;
    currentpicture := nullpicture;

    color c[];
    c[0] := black;
    c[1] := black;
    c[2] := black;
    c[3] := black;

    picture p[], legenda;
    legenda := nullpicture;
    numeric nsat[];
    nsat[0] := 16;
    nsat[1] := 6;
    nsat[2] := 2;
    nsat[3] := 1;

    
    p[0] := nullpicture;
    addto p[0] also btex $n_{\text{start}}=16$ etex 
	withcolor c[0];
    p[1] := nullpicture;
    addto p[1] also btex $n_{\text{start}}=6$ etex 
	withcolor c[1];
    p[2] := nullpicture;
    addto p[2] also btex $n_{\text{start}}=2$ etex 
	withcolor c[2];
    p[3] := nullpicture;
    addto p[3] also btex $n_{\text{start}}=1$ etex 
	withcolor c[3];

    for i:= 0 upto 3:
	addto legenda doublepath ( (0,0)--(5mm,0) ) 
	    shifted (0, -5mm*(3 - i))
	    withpen currentpen withcolor c[i];
        addto legenda also marker[i]
	    shifted (0, -5mm*(3 - i));
        addto legenda also marker[i]
	    shifted (5mm, -5mm*(3 - i));
	addto legenda also p[i]
	    shifted 1/2(lrcorner p[0] - ulcorner p[0])
	    shifted (0, -5mm*(3 - i));
    endfor;

 
    draw begingraph(3in, 1.5in);
        glabel.bot(btex \small Вероятность удаления сценария $p_{\text{thin}} \times 100 \%$ etex, OUT);
        glabel.lft(btex \small Медианное время, c. etex rotated 90, OUT);
        for i=0 upto 3:
            gdraw decimal(nsat[i]) & ".dat" withcolor c[i] withpen pencircle scaled 1pt;
	    path p;
	    gdata(decimal(nsat[i]) & ".dat", s, augment.p(s1, s2); );
	    N:=length p; 
	    for n=0 upto N: 
		x:=xpart point n of p; 
		y:=ypart point n of p; 
		glabel(marker[i], x, y);
	    endfor; 
        endfor;
        
%        autogrid(otick.bot, otick.lft) withcolor .85white;
        autogrid(, otick.lft);

        glabel.rt(legenda, OUT);
	otick.bot(btex 0 etex, 0);
	otick.bot(btex 0.1 etex, 0.1);
	otick.bot(btex 0.2 etex, 0.2);
	otick.bot(btex 0.3 etex, 0.3);
	otick.bot(btex 0.4 etex, 0.4);
	otick.bot(btex 0.5 etex, 0.5);
	otick.bot(btex 0.6 etex, 0.6);
	otick.bot(btex 0.7 etex, 0.7);
	otick.bot(btex 0.8 etex, 0.8);
	otick.bot(btex 0.9 etex, 0.9);


    endgraph;

endfig;

end.
