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

Essa parte foi uma das mais desafiados, eu queria implementar uma transaction para a conexão de amizadades porque eu teria que fazer queries antes de de fszer a conexão para não enviar a conexão mais de uma vez. Além disso eu tinha que bloquear commits entre os dois usuários enquanto uma solicitação de conexão está sendo feita. Cada usuário usáiro pode ter no máximo 500 conexões e o usuário só pode enviar conexão para membros do grupo, eu acabai não implementando a saida do usuário do grupo. Pensando na transaction em java você não pode passar um função de callback por parâmetro, então para fazer isso eu criei uma classe chamada TransactionManager que tem o método run que recebe uma função lambda. O método run tem uma interface com o método execute, ou seja, o run recebe uma função, que no caso seria a createRequest e por debaixo dos panos, o java coloca o createRequest dentro do método execute, assim o createRequest não é executado na hora,  o método run em TransactionManager é que executa o método execute depois de ter criado a transaction. Eu não sabia desse recurso em java e pesquisando vi como implementar é algo bem interessante, embora não seja tão prático, outras lingunagens como JavaScript ou PHP são melhores para fazer isso.

### Chat 

Eu implementei um o chat virtual com um porém, como no js eu estou usando o socket.io que é feito em js eu tive que criar o servidor em node.js, porém toda a lógica do fluxo de chat é feito em javalin, só o servidor e a conexão é feita em node. Então do front vai para API javalin para pegar a sessão e o front envia pro servidor. a partir dai que a conexão já foi criada, o envio de mensagens funciona da seguinte forma: o front envia a mensagem pro servidor node e o servidor envia o payload para a API do javalin e a mensagem é salva no banco. Essa foi a única forma mais ou menos decente que eu consegui fazer


#;# URL 
https://final-2026a-samuel.vercel.app/

Rede social para grupos de amigos registrarem exercícios, participarem de
desafios, comentarem check-ins e conversarem em tempo real.

## Desenvolvimento local

1. Copie `.env.example` para `.env`.
2. Execute:

```bash
docker compose up --build
```

3. Em outro terminal:

```bash
cd frontend
yarn install
yarn dev
```

Frontend: http://localhost:5173

API: http://localhost:7000

Chat: http://localhost:7001
