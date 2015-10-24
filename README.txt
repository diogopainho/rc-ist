====================== Identificação do grupo ======================
Projeto do Grupo 41
Diogo Painho Rodrigues - 73245
Natalino António Bernardino Cordeiro - 74117
Miguel Francisco Vicente da Fonseca - 67040

====================== Manual de Instruções ======================


Dentro da pasta CS existem dois ficheiros, o ssinfo.txt e o filesinfo.txt. Estes ficheiros são obrigatório para o CS funcionar em condições.

No ssinfo.txt deve-se colocar a informação da seguinte forma:
<ipclient1> <ipss1> <portss1>
<ipclient2> <ipss2> <portss2>

Onde o <ipclientn> se associa e só estabelece ligação com o <ipssn>

No filesinfo.txt devem estar os nomes dos ficheiros que estão nos SS. Se os SS iniciarem vazios o ficheiro deve estar vazio. Caso contrário, deve ser escrito o ficheiro da seguinte forma:
<filename1>
<filename2>

Sem "<" e “>”.

Para correr o projeto, basta compilar usando o comando make dentro da pasta proj41.
Para o caso do Client deve-se fazer "java user.java" dentro user.
Para o caso do CS deve-se fazer "java CS.java" dentro da pasta CS.
Para o caso do SS deve-se fazer "java SS.java" dentro da pasta SS.

Atenção que deve-se actualizar o ficheiro ssinfo.txt com os devidos IP's para os Clientes e Storage Servers, como explicado anteriormente.
É tambem necessário alterar o código do CS nas linhas 110 e 111 para defenir um ip e porto do SS default que é devolvido quando o cliente não é reconhecido pelo servidor.