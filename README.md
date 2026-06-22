# GymSocial

**Nome:** Samuel Souza Pereira Nunes  
**Curso:** Sistemas de Informação

## Tema 
O objetivo é criar uma rede social de academia, onde amigos se juntam e montam um grupo e compartilham fotos dos seus treinos. Os grupos podem montar desafios e quem tiver mais check-ins vence. 
Os integrantes do grupo pode postar check-ins, comentar em chek-ins, se conectar com pessoas do grupo, os integrantes do grupo podem conversar em tempo.


## Processo de desenvolvimento
O front-end foi feito em react e o back-end é uma api em javalin, o banco de dados é PostgreSQL, o HikariCP para conexão com o banco e o Flyway para gerenciar os schemas do banco.

Vou citar os fluxos que eu acredito serem mais legais ou importantes 

## Fluxo de autenticação

A autenticação foi feita em JWT e Refresh Token Rotation.
No react temos esse componente AuthGuard que controla o fluxo de acesso no front-end.
```
import { Dumbbell } from "lucide-react";
import { useEffect, useState } from "react";
import {
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";
import { restoreSession } from "../../../services/apiClient";
import { buildAuthPath, getAuthRedirect } from "../services/authRedirect";

type AuthGuardProps = {
  mode: "require" | "guest";
};

type AuthStatus = "checking" | "authenticated" | "guest";

function AuthGuard({ mode }: AuthGuardProps) {
  const location = useLocation();
  const [status, setStatus] = useState<AuthStatus>("checking");

  useEffect(() => {
    let active = true;

    restoreSession()
      .then((authenticated) => {
        if (active) {
          setStatus(authenticated ? "authenticated" : "guest");
        }
      })
      .catch(() => {
        if (active) {
          setStatus("guest");
        }
      });

    return () => {
      active = false;
    };
  }, []);

  if (status === "checking") {
    return <AuthGuardLoading />;
  }

  if (mode === "require" && status === "guest") {
    const currentPath = `${location.pathname}${location.search}`;
    return (
      <Navigate
        to={buildAuthPath("/login", currentPath)}
        replace
      />
    );
  }

  if (mode === "guest" && status === "authenticated") {
    return (
      <Navigate
        to={getAuthRedirect(location.search)}
        replace
      />
    );
  }

  return <Outlet />;
}

export function GuestOnly() {
  return <AuthGuard mode="guest" />;
}

export function RequireAuth() {
  return <AuthGuard mode="require" />;
}

function AuthGuardLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <div className="text-center">
        <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
          <Dumbbell size={26} />
        </span>
        <p className="mt-4 text-sm font-bold text-zinc-500">
          Verificando sua sessão...
        </p>
      </div>
    </main>
  );
}
```

Eu usei o React Router para o roteamento no front. Tem rotas que são guest, as rotas de login e registro, e as rotas auth, que o usuário só pode acessar se estiver autenticado, as rotas require. O componente também proíbe o usuário já logado entrar nas rotas guest. Se o usuário não autenticado tentar entrar em uma rota require, ele é redirecionado para login, e se o usuário autenticado tentar acessar uma rota guest, ele volta para a rota que ele estava antes.

### O fluxo foi pensando e implementado dessa maneira:

Temos o apiClient, que intercepta todas as requests. Ela é responsável por colocar o accessToken no header das requisições e ele também faz a request de refresh para quando o accessToken expira e é necessário criar outro. O accessToken é enviado nos headers de toda requisição, exceto nas requests de login, register e refresh. Nessas requests, o accessToken é enviado no response e é salvo em memória. Eu já tinha implementado autenticações JWT antes, porém com algumas diferenças dessa implementação nesse projeto. Acredito que a implementação de autenticação nesse projeto foi a melhor que eu fiz até o momento, porque nas outras implementações o accessToken era salvo no localStorage e o refreshToken não sofria rotação. Agora o accessToken é salvo em memória, o refreshToken é salvo nos cookies com httpOnly, assim evita que algum código JS malicioso pegue o refreshToken e, além disso, o refreshToken sofre rotação, ou seja, quando o refreshToken é usado mais de uma vez, é criado outro refreshToken e esse novo é o que passa a valer. Eu tive conhecimento da existência do refreshToken rotation criando esse projeto. Confesso que, de início, fiquei meio relutante com a ideia porque, na minha cabeça, não tinha tanto sentido ser jogado fora tão rapidamente assim; ele deveria ter um tempo de expiração maior e ser usado várias e várias vezes para gerar um novo accessToken. Mas, lendo em fóruns e pesquisando, vi que a rotação é algo válido e mais seguro, já que, se um atacante descobre o refresh token, o poder de ataque diminui drasticamente, porque logo logo ele vai deixar de ser válido. Sem a rotação, o atacante poderia renovar a sessão dele várias e várias vezes.

Essa parte foi bem desafiadora porque, como o accessToken está sendo salvo em memória, ele se perde muito facilmente; basta eu dar refresh na página. Dessa forma, foi bem chato fazer isso funcionar, então a forma que foi feita é que, quando acontece refresh na página, o AuthGuard chama o método restoreSession no ApiClient e cria um novo accessToken. No back, temos uma middleware chamada JwtAuthenticationMiddleware, que valida o token. 

Antes de entrar no fluxo de autenticação no back-end, vou falar como funciona a estrutura dele.

Organizei a criação e composição das dependências nos arquivos de configuração. Não é igual ao Spring Boot, que automatiza esse processo por meio de um container de injeção de dependências; nesse projeto, a composição é feita manualmente. As classes permanecem organizadas nos diretórios correspondentes às suas responsabilidades, enquanto os arquivos de configuração centralizam a criação das instâncias e a passagem de suas dependências. Dessa forma, o acoplamento fica mais explícito e a aplicação mantém uma estrutura semelhante à injeção de dependências, porém sem framework.

Tem o próprio Application.java, que é responsável por startar o servidor, e dentro da main tem a classe ApplicationConfig, que se concentra na leitura e validação das variáveis de ambiente. Essa parte foi bem chata de se fazer, para fazer algo mais automátizado eu teria que ter mais conhecimento e tempo para fazer.

Dentro do Application também tem o ApplicationModule, que é o ponto central de composição da aplicação. Nele são criadas as dependências compartilhadas, como DataSource, ImageStorage e PublicIdCodec, que são passadas explicitamente para os módulos de cada domínio. Cada módulo, como AuthModule, instancia seus repositórios, serviços, controllers e middlewares, conectando suas dependências por construtor. Os módulos retornam apenas os componentes necessários para a configuração das rotas. Por fim, JavalinConfig e RouteConfig recebem esses componentes para configurar o servidor e registrar os endpoints. Agora, podendo voltar para o fluxo de autenticação, a middleware é instanciada na classe de configuração rotas, RouteConfig. Ela é chamada nas rotas que precisam de validação. Além disso, eu criei uma classe chamada AuthenticatedUserContext, que é a mesma ideia do SecurityContextHolder do Spring Security.

### Fluxo de uplaod de imagens
Como imagens são bem presentes nessa aplicação, acredito que falar sobre isso é importante. eu Criei um bucket no Minio para armazenar as imagens e no banco de dados de dados somente as informações das imagens são salvas no banco. O upload de imagens é feito de uma vez, não é por partes então eu limitei o tamanho da imagem e antes de enviar para o back-end eu faço a compressão. Eu faço a validação no front e no back-end para que não tenha risco de alguém enviar um arquivo.php malicioso por exemplo, então os tipos válidos são png, jpg e webp eu válido tanto no back quanto no front.


### Fluxo de envio de amizades

Essa parte foi uma das mais desafiadoras. Eu queria implementar uma transação para a conexão de amizades, porque eu teria que fazer queries antes de fazer a conexão para não enviar a conexão mais de uma vez. Além disso, eu tinha que bloquear commits entre os dois usuários enquanto uma solicitação de conexão estava sendo feita. Cada usuário pode ter no máximo 500 conexões, e o usuário só pode enviar conexão para membros do grupo. Eu acabei não implementando a saída do usuário do grupo. Pensando na transação em Java, você não pode passar uma função de callback por parâmetro, então, para fazer isso, eu criei uma classe chamada TransactionManager que tem o método run, que recebe uma função lambda. O método run tem uma interface com o método execute, ou seja, o run recebe uma função, que no caso seria a createRequest, e, por debaixo dos panos, o Java coloca a createRequest dentro do método execute. Assim, a createRequest não é executada na hora; o método run em TransactionManager é que executa o método execute depois de ter criado a transaction. Eu não sabia desse recurso em Java e, pesquisando, vi como implementá-lo. É algo bem interessante, embora não seja tão prático; outras linguagens como JavaScript ou PHP são melhores para fazer isso.

### Chat 

Eu implementei um chat virtual com um porém: como no JS eu estou usando o socket.io, que é feito em JS, eu tive que criar o servidor em Node.js. Porém, toda a lógica do fluxo de chat é feita em Javalin; só o servidor e a conexão são feitos em Node Então, do front vai para A API Javalin para pegar a sessão, e o front envia para o servidor. A partir daí, que a conexão já foi criada, o envio de mensagens funciona da seguinte forma: o front envia a mensagem para o servidor Node, e o servidor envia o payload para a API do Javalin, e a mensagem é salva no banco. Essa foi a única forma mais ou menos decente que eu consegui fazer.

Para encerar o processo de desenvolvimento, vou citar duas coisas interessantes: paginação e hashids.

### Paginação
As mensagens do chat, listagem de check-ins e amizades usam paginação. Acredito que a paginação precisa de melhorias em dois quesitos: a paginação deveria ser mais centralizada, acontece bastante repetição desnecessária e somente os check-ins e amizades usam scroll infinito. No chat é necessário clicar em um botão para abrir mais mensagens, porém essas correções não vão ser feitas até o dia 22 de junho, infelizmente. 

Mas, voltando para a paginação, eu pensei em implementar a "simple paginate", ou seja, ela não conta o número total de registros; a simple paginate usa o HasMorePages para saber se existem mais páginas ou não. Funciona assim: na query é buscado o pageSize + 1. Se results.size() for maior que pageSize, significa que existem mais registros disponíveis além dos retornados na página atual. Nesse caso, hasMorePages deve ser definido como true. Mas, antes de enviar para o front, faço um split para não retornar o último registro. Dessa forma, não agride o servidor contando todos os registros no banco. 

### Hashids

Atualmente, muitas APIs não usam mais o id, e sim UUID, porque é muito perigoso expor o id do usuário no front. Então, em vez de mandar o id para o front, é mandado o UUID, porém o custo de leitura e escrita é bem maior. O PostgreSQL tem uma forma de diminuir esse custo. Eu li um artigo sobre isso, mas não estou encontrando ele mais. Dessa forma, como a maioria das entidades não vai ter muita leitura ou escrita, deixei todas com UUID, exceto a tabela de usuários, que usa ID. Então o ID é hasheado no front; a classe "PublicIdCodec" é que cuida disso. Quem é responsável por codificar o ID é a lib Hashids, que é do próprio Java. Embora não seja a melhor solução do mundo, é melhor do que expor o id no front.


## Diagrama de classes 
Eu usei o mermaid para criar o diagrama de classes porque ele permite criar diagramas usando marckdown e é gratuito. Como o diagrama ficou muito grande eu coloquei no diretório docs/diagramas


## Uso de IA
Eu basicamente usei codex e chatGPT para criar tudo, eles me ajudaram na criação dos testes, implementações etc. O codex me ajudou a montar a estrutura de diretórios, ele criou o diagrama de classes em markdown. 


## Orientações para execução: instalação de dependências

1. Copie `.env.example` para `.env`.
2. Execute:

```bash
docker compose up --build -d
```

3. Para rodar o projeto no front-end:

```bash
cd frontend
yarn install
yarn dev
```

Frontend: http://localhost:5173

API: http://localhost:7000

Chat: http://localhost:7001

A url de producção é essa: https://final-2026a-samuel.vercel.app/


## Resultado final
<img src="./docs/video.gif" width="900">


## Fontes e cŕeditos
https://www.devmedia.com.br/como-usar-funcoes-lambda-em-java/32826
https://mermaid.live/edit
https://dev.to/danielasaboro/security-isnt-all-rosy-what-i-learnt-from-participating-in-treblle-api-hackathon-1kok
https://auth0.com/docs/secure/tokens/refresh-tokens/refresh-token-rotation
